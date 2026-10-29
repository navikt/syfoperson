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
import no.nav.syfo.naeringskode.Naeringskode
import no.nav.syfo.naeringskode.NaeringskodeStandard

class SsbClient(
    private val valkeyStore: ValkeyStore,
    private val baseUrl: String,
    private val httpClient: HttpClient = httpClientDefault(),
) {
    private val cacheLocksByStandard = NaeringskodeStandard.entries.associateWith { Mutex() }

    suspend fun getNaeringskoder(standard: NaeringskodeStandard): List<Naeringskode> {
        val cacheKey = "ssb:naeringskoder:${standard.year}"
        val cached = valkeyStore.getListObject<Naeringskode>(cacheKey)
        if (!cached.isNullOrEmpty()) {
            return cached
        }

        return cacheLocksByStandard.getValue(standard).withLock {
            // Another request may have populated the cache while this one waited.
            val cachedAfterLock = valkeyStore.getListObject<Naeringskode>(cacheKey)
            if (!cachedAfterLock.isNullOrEmpty()) {
                return@withLock cachedAfterLock
            }

            val naeringskoder = try {
                val response = httpClient.get("$baseUrl/versions/${standard.ssbVersion}") {
                    parameter("language", "nb")
                    accept(ContentType.Application.Json)
                }
                check(response.status == HttpStatusCode.OK) { "Unexpected SSB status: ${response.status}" }
                toNaeringskoder(response.body<SsbVersion>(), standard)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw SsbException("Failed to fetch naeringskoder for SN${standard.year}", e)
            }
            valkeyStore.setObject(
                expireSeconds = 60 * 60 * 24L,
                key = cacheKey,
                value = naeringskoder,
            )
            naeringskoder
        }
    }

    private fun toNaeringskoder(version: SsbVersion, standard: NaeringskodeStandard): List<Naeringskode> {
        require(version.id == standard.ssbVersion) {
            "Unexpected SSB classification version"
        }
        val naeringskoder = version.classificationItems.map { item ->
            val code = item.code.trim()
            val name = item.name.trim()
            val parentCode = item.parentCode?.trim()?.ifEmpty { null }
            val level = item.level
            require(code.isNotBlank() && name.isNotBlank() && level in 1..5)
            require((level == 1) == (parentCode == null)) { "Missing SSB parent code" }
            Naeringskode(
                code = code,
                name = name,
                parentCode = parentCode,
                shortName = item.shortName?.trim()?.ifEmpty { null },
            )
        }
        require(naeringskoder.isNotEmpty()) { "Empty SSB hierarchy" }
        val byCode = naeringskoder.associateBy { it.code }
        require(byCode.size == naeringskoder.size) { "Duplicate SSB codes" }
        naeringskoder.forEach { naeringskode ->
            val visited = mutableSetOf<String>()
            var current: Naeringskode? = naeringskode
            while (current != null) {
                require(visited.add(current.code)) { "Cycle in SSB hierarchy" }
                current = current.parentCode?.let { parentCode ->
                    requireNotNull(byCode[parentCode]) { "Incomplete SSB hierarchy" }
                }
            }
        }
        return naeringskoder
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
