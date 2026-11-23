package no.nav.syfo.client.ssb

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.commonConfig
import no.nav.syfo.sektorkode.Sektorkode
import no.nav.syfo.sektorkode.SektorkodeStandard
import no.nav.syfo.util.configuredJacksonMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SsbClientSektorkodeTest {
    private val cache = mutableMapOf<String, String>()
    private val mapper = configuredJacksonMapper()
    private val valkeyStore = mockk<ValkeyStore>(relaxed = true).also { store ->
        every { store.objectMapper } returns mapper
        every { store.get(any()) } answers { cache[firstArg()] }
        every { store.setObject(any(), any(), any<List<Sektorkode>>()) } answers {
            cache[secondArg()] = mapper.writeValueAsString(thirdArg<List<Sektorkode>>())
        }
    }

    @Test
    fun `parses both SSB versions and caches each standard for one day`() = runBlocking {
        val requestedPaths = mutableListOf<String>()
        val client = SsbClient(
            valkeyStore = valkeyStore,
            baseUrl = "https://ssb.test",
            httpClient = HttpClient(
                MockEngine { request ->
                    requestedPaths.add(request.url.encodedPath)
                    assertEquals("nb", request.url.parameters["language"])
                    assertEquals("application/json", request.headers[HttpHeaders.Accept])
                    val standard = SektorkodeStandard.entries.single {
                        request.url.encodedPath == "/versions/${it.ssbVersion}"
                    }
                    respond(
                        hierarchyJson(standard),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ) { commonConfig() },
        )

        SektorkodeStandard.entries.forEach { standard ->
            val result = client.getSektorkoder(standard)
            assertEquals(expectedHierarchy(standard), result)
            assertEquals(result, client.getSektorkoder(standard))
            verify(exactly = 1) {
                valkeyStore.setObject(86400L, "ssb:sektorkoder:${standard.year}", result)
            }
        }
        assertEquals(listOf("/versions/93", "/versions/92"), requestedPaths)
    }

    @Test
    fun `concurrent cache misses fetch a standard only once`() = runBlocking {
        var requests = 0
        val client = SsbClient(
            valkeyStore = valkeyStore,
            baseUrl = "https://ssb.test",
            httpClient = HttpClient(
                MockEngine {
                    requests++
                    delay(20)
                    respond(
                        hierarchyJson(SektorkodeStandard.IS2012),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ) { commonConfig() },
        )

        val results = List(5) { async { client.getSektorkoder(SektorkodeStandard.IS2012) } }.awaitAll()

        assertEquals(1, requests)
        assertTrue(results.all { it == expectedHierarchy(SektorkodeStandard.IS2012) })
    }

    @Test
    fun `rejects invalid SSB hierarchies without caching`() = runBlocking {
        val valid = hierarchyJson(SektorkodeStandard.IS2012)
        val invalidResponses = listOf(
            valid.replace("\"id\":92", "\"id\":93"),
            valid.replace("\"name\":\"Innenlandske sektorer\"", "\"name\":\"  \""),
            valid.replace("\"level\":\"3\"", "\"level\":\"invalid\""),
            valid.replace("\"parentCode\":\"INNL\"", "\"parentCode\":\"MISSING\""),
            valid.replace("\"code\":\"6100\"", "\"code\":\"C_OFF\""),
            """
                {
                    "id":92,
                    "classificationItems":[
                        {"code":"INNL","parentCode":"C_OFF","level":"1","name":"Root"},
                        {"code":"C_OFF","parentCode":"INNL","level":"2","name":"Cycle"}
                    ]
                }
            """.trimIndent(),
            """{"id":92,"classificationItems":[]}""",
        )
        var requests = 0
        invalidResponses.forEach { body ->
            val client = SsbClient(
                valkeyStore = valkeyStore,
                baseUrl = "https://ssb.test",
                httpClient = HttpClient(
                    MockEngine {
                        requests++
                        respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                    },
                ) { commonConfig() },
            )

            assertThrows(SsbException::class.java) {
                runBlocking { client.getSektorkoder(SektorkodeStandard.IS2012) }
            }
        }
        assertEquals(invalidResponses.size, requests)
        assertTrue(cache.isEmpty())
        verify(exactly = 0) { valkeyStore.setObject(any(), any(), any<List<Sektorkode>>()) }
    }

    @Test
    fun `corrupt cached JSON fails without requesting or rewriting the cache`() = runBlocking {
        cache["ssb:sektorkoder:2012"] = "{broken"
        var requests = 0
        val client = SsbClient(
            valkeyStore = valkeyStore,
            baseUrl = "https://ssb.test",
            httpClient = HttpClient(
                MockEngine {
                    requests++
                    respond(
                        hierarchyJson(SektorkodeStandard.IS2012),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ) { commonConfig() },
        )

        assertThrows(SsbException::class.java) {
            runBlocking { client.getSektorkoder(SektorkodeStandard.IS2012) }
        }

        assertEquals(0, requests)
        verify(exactly = 0) { valkeyStore.setObject(any(), any(), any<List<Sektorkode>>()) }
    }

    private fun expectedHierarchy(standard: SektorkodeStandard): List<Sektorkode> = when (standard) {
        SektorkodeStandard.IS1987 -> listOf(
            Sektorkode("ALLENO", "Norske sektorer i alt", null, 1),
            Sektorkode("A_OFF", "Offentlig forvaltning", "ALLENO", 2),
            Sektorkode("110", "Stats- og trygdeforvaltningen", "A_OFF", 3),
            Sektorkode("C_IKKE-FIN", "Ikke-finansiell sektor", "ALLENO", 2),
        )

        SektorkodeStandard.IS2012 -> listOf(
            Sektorkode("INNL", "Innenlandske sektorer", null, 1),
            Sektorkode("C_OFF", "Offentlig forvaltning", "INNL", 2),
            Sektorkode("6100", "Statsforvaltningen", "C_OFF", 3),
        )
    }

    private fun hierarchyJson(standard: SektorkodeStandard): String {
        val items = when (standard) {
            SektorkodeStandard.IS1987 -> listOf(
                Triple("ALLENO", null, "Norske sektorer i alt"),
                Triple("A_OFF", "ALLENO", "Offentlig forvaltning"),
                Triple("110", "A_OFF", "Stats- og trygdeforvaltningen"),
                Triple("C_IKKE-FIN", "ALLENO", "Ikke-finansiell sektor"),
            )

            SektorkodeStandard.IS2012 -> listOf(
                Triple("INNL", null, "Innenlandske sektorer"),
                Triple("C_OFF", "INNL", "Offentlig forvaltning"),
                Triple("6100", "C_OFF", "Statsforvaltningen"),
            )
        }
        val jsonItems = items.joinToString(",") { (code, parent, name) ->
            val parentCode = parent?.let { "\"$it\"" } ?: "\"\""
            val level = when (code) {
                "ALLENO", "INNL" -> 1
                "A_OFF", "C_OFF", "C_IKKE-FIN" -> 2
                else -> 3
            }
            """{"code":"$code","parentCode":$parentCode,"level":"$level","name":"$name"}"""
        }
        return """{"id":${standard.ssbVersion},"classificationItems":[$jsonItems]}"""
    }
}
