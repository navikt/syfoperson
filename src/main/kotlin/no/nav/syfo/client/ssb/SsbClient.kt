package no.nav.syfo.client.ssb

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.naringskode.Naringskode
import no.nav.syfo.naringskode.NaringskodeStandard

class SsbClient(
    private val valkeyStore: ValkeyStore,
    private val baseUrl: String,
    private val httpClient: HttpClient = httpClientDefault(),
) {
    private val cacheLocksByStandard = NaringskodeStandard.entries.associateWith { Mutex() }

    suspend fun getNaringskoder(standard: NaringskodeStandard): List<Naringskode> {
        val cacheKey = "ssb:naringskoder:${standard.year}"
        val cached = valkeyStore.getListObject<Naringskode>(cacheKey)
        if (!cached.isNullOrEmpty()) {
            return cached
        }

        return cacheLocksByStandard.getValue(standard).withLock {
            // Another request may have populated the cache while this one waited.
            val cachedAfterLock = valkeyStore.getListObject<Naringskode>(cacheKey)
            if (!cachedAfterLock.isNullOrEmpty()) {
                return@withLock cachedAfterLock
            }

            val naringskoder = try {
                val response = httpClient.get("$baseUrl/versions/${standard.ssbVersion}") {
                    parameter("language", "nb")
                    accept(ContentType.Application.Json)
                }
                check(response.status == HttpStatusCode.OK) { "Unexpected SSB status: ${response.status}" }
                toNaringskoder(response.body<SsbVersion>(), standard)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw SsbException("Failed to fetch naringskoder for SN${standard.year}", e)
            }
            valkeyStore.setObject(
                expireSeconds = 60 * 60 * 24L,
                key = cacheKey,
                value = naringskoder,
            )
            naringskoder
        }
    }

    private fun toNaringskoder(version: SsbVersion, standard: NaringskodeStandard): List<Naringskode> {
        require(version.id == standard.ssbVersion) {
            "Unexpected SSB classification version"
        }
        val naringskoder = version.classificationItems.map { item ->
            val code = item.code.trim()
            val name = item.name.trim()
            val parentCode = item.parentCode?.trim()?.ifEmpty { null }
            val level = item.level
            require(code.isNotBlank() && name.isNotBlank() && level in 1..standard.levels)
            require((level == 1) == (parentCode == null)) { "Missing SSB parent code" }
            Naringskode(
                code = code,
                name = name,
                parentCode = parentCode,
                shortName = item.shortName?.trim()?.ifEmpty { null },
            )
        }
        require(naringskoder.isNotEmpty()) { "Empty SSB hierarchy" }
        val byCode = naringskoder.associateBy { it.code }
        require(byCode.size == naringskoder.size) { "Duplicate SSB codes" }
        naringskoder.forEach { naringskode ->
            val visited = mutableSetOf<String>()
            var current: Naringskode? = naringskode
            while (current != null) {
                require(visited.add(current.code)) { "Cycle in SSB hierarchy" }
                current = current.parentCode?.let { parentCode ->
                    requireNotNull(byCode[parentCode]) { "Incomplete SSB hierarchy" }
                }
            }
        }
        return naringskoder
    }
}

data class SsbVersion(
    val id: Int,
    val classificationItems: List<SsbClassificationItem>,
)

data class SsbClassificationItem(
    val code: String,
    val name: String,
    val parentCode: String? = null,
    val level: Int,
    val shortName: String? = null,
)

class SsbException(message: String, cause: Throwable) : RuntimeException(message, cause)
