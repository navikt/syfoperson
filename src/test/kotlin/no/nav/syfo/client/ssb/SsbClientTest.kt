package no.nav.syfo.client.ssb

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.commonConfig
import no.nav.syfo.naringskode.Naringskode
import no.nav.syfo.naringskode.NaringskodeStandard
import no.nav.syfo.util.configuredJacksonMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SsbClientTest {
    private val cache = mutableMapOf<String, String>()
    private val mapper = configuredJacksonMapper()
    private val valkeyStore = mockk<ValkeyStore>(relaxed = true).also { store ->
        every { store.objectMapper } returns mapper
        every { store.get(any()) } answers { cache[firstArg()] }
        every { store.setObject(any(), any(), any<List<Naringskode>>()) } answers {
            cache[secondArg()] = mapper.writeValueAsString(thirdArg<List<Naringskode>>())
        }
    }

    @Test
    fun `fetches JSON for all standards and caches each hierarchy for one day`() = runBlocking {
        val requestedPaths = mutableListOf<String>()
        val client = SsbClient(
            valkeyStore = valkeyStore,
            baseUrl = "https://ssb.test",
            httpClient = HttpClient(
                MockEngine { request ->
                    requestedPaths.add(request.url.encodedPath)
                    assertEquals("nb", request.url.parameters["language"])
                    assertEquals("application/json", request.headers[HttpHeaders.Accept])
                    val standard = NaringskodeStandard.entries.single {
                        request.url.encodedPath == "/versions/${it.ssbVersion}"
                    }
                    respond(
                        hierarchyJson(standard),
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            ) { commonConfig() },
        )

        NaringskodeStandard.entries.forEach { standard ->
            val first = client.getNaringskoder(standard)
            assertEquals(standard.levels, first.size)
            assertEquals(if (standard == NaringskodeStandard.SN2007) "J" else "K", first.last().code)
            assertNull(first.last().parentCode)
            assertEquals(first, client.getNaringskoder(standard))
            verify(exactly = 1) {
                valkeyStore.setObject(86400L, "ssb:naringskoder:${standard.year}", first)
            }
        }
        assertEquals(listOf("/versions/32", "/versions/31", "/versions/30", "/versions/3218"), requestedPaths)
    }

    @Test
    fun `accepts missing null and blank short names`() = runBlocking {
        val body = hierarchyJson(NaringskodeStandard.SN2025).replace("\"shortName\":\"Short 62.200\",", "")
            .replace("\"shortName\":\"Short 62.20\"", "\"shortName\":null")
            .replace("\"shortName\":\"Short 62.2\"", "\"shortName\":\"  \"")
        val client = SsbClient(
            valkeyStore = valkeyStore,
            baseUrl = "https://ssb.test",
            httpClient = HttpClient(
                MockEngine {
                    respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                }
            ) { commonConfig() },
        )

        val result = client.getNaringskoder(NaringskodeStandard.SN2025)

        assertEquals(listOf(null, null, null, "Short 62", "Short K"), result.map { it.shortName })
        assertEquals(result, client.getNaringskoder(NaringskodeStandard.SN2025))
    }

    @Test
    fun `concurrent cache misses only fetch the hierarchy once`() = runBlocking {
        var requests = 0
        val client = SsbClient(
            valkeyStore = valkeyStore,
            baseUrl = "https://ssb.test",
            httpClient = HttpClient(
                MockEngine {
                    requests++
                    delay(20)
                    respond(
                        hierarchyJson(NaringskodeStandard.SN2025),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            ) { commonConfig() },
        )

        val results = List(5) { async { client.getNaringskoder(NaringskodeStandard.SN2025) } }.awaitAll()

        assertEquals(1, requests)
        assertTrue(results.all { it == results.first() })
    }

    @Test
    fun `cache hits do not wait for an ongoing SSB request`() = runBlocking {
        val requestStarted = CompletableDeferred<Unit>()
        val releaseRequest = CompletableDeferred<Unit>()
        val client = SsbClient(
            valkeyStore = valkeyStore,
            baseUrl = "https://ssb.test",
            httpClient = HttpClient(
                MockEngine {
                    requestStarted.complete(Unit)
                    releaseRequest.await()
                    respond(
                        hierarchyJson(NaringskodeStandard.SN2025),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            ) { commonConfig() },
        )
        val pendingRequest = async { client.getNaringskoder(NaringskodeStandard.SN2025) }
        try {
            withTimeout(5_000) { requestStarted.await() }
            val cached = listOf(Naringskode("K", "Name K", null, "Short K"))
            cache["ssb:naringskoder:2025"] = mapper.writeValueAsString(cached)

            val result = withTimeout(5_000) { client.getNaringskoder(NaringskodeStandard.SN2025) }

            assertEquals(cached, result)
        } finally {
            releaseRequest.complete(Unit)
        }
        assertEquals(5, pendingRequest.await().size)
    }

    @Test
    fun `does not cache failed or invalid SSB responses`() = runBlocking {
        val valid = hierarchyJson(NaringskodeStandard.SN2025)
        val responses = listOf(
            HttpStatusCode.ServiceUnavailable to "Unavailable",
            HttpStatusCode.OK to "{broken",
            HttpStatusCode.OK to valid.replace("\"parentCode\":\"62.20\"", "\"parentCode\":\"99\""),
            HttpStatusCode.OK to valid.replace("\"parentCode\":\"62.20\"", "\"parentCode\":\"62.200\""),
            HttpStatusCode.OK to valid.replace("\"parentCode\":\"62.20\"", "\"parentCode\":null"),
            HttpStatusCode.OK to valid.replace("\"id\":3218", "\"id\":30"),
            HttpStatusCode.OK to """{"id":3218,"classificationItems":[]}""",
            HttpStatusCode.OK to valid.replace("\"name\":\"Name 62.200\"", "\"name\":\"\""),
            HttpStatusCode.OK to valid.replace("\"name\":\"Name 62.200\",", ""),
            HttpStatusCode.OK to valid.replace("\"level\":\"5\"", "\"level\":\"invalid\""),
            HttpStatusCode.OK to valid.replace("\"level\":\"5\"", "\"level\":\"6\""),
        )
        responses.forEach { (status, body) ->
            val client = SsbClient(
                valkeyStore = valkeyStore,
                baseUrl = "https://ssb.test",
                httpClient = HttpClient(
                    MockEngine {
                        respond(body, status, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                    }
                ) { commonConfig() },
            )
            try {
                client.getNaringskoder(NaringskodeStandard.SN2025)
                fail<Unit>("Expected SsbException")
            } catch (_: SsbException) {
                assertTrue(cache.isEmpty())
            }
        }
        verify(exactly = 0) { valkeyStore.setObject(any(), any(), any<List<Naringskode>>()) }
    }

    private fun hierarchyJson(standard: NaringskodeStandard): String {
        val codes = when (standard) {
            NaringskodeStandard.SN1994, NaringskodeStandard.SN2002 ->
                listOf("72.100", "72.10", "72.1", "72", "K_", "K")
            NaringskodeStandard.SN2007 -> listOf("62.020", "62.02", "62.0", "62", "J")
            NaringskodeStandard.SN2025 -> listOf("62.200", "62.20", "62.2", "62", "K")
        }
        val items = codes.mapIndexed { index, code ->
            val parentCode = codes.getOrNull(index + 1)?.let { "\"$it\"" } ?: "null"
            """
            {
                "code":"$code","parentCode":$parentCode,
                "level":"${codes.size - index}","name":"Name $code","shortName":"Short $code","notes":"Ignored"
            }
            """.trimIndent()
        }.joinToString(",")
        return """
            {
                "id":${standard.ssbVersion},
                "name":"Ignored version name",
                "classificationItems":[$items]
            }
        """.trimIndent()
    }
}
