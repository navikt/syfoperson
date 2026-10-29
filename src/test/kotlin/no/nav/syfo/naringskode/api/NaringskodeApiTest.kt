package no.nav.syfo.naringskode.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.syfo.client.ssb.SsbException
import no.nav.syfo.naringskode.Naringskode
import no.nav.syfo.naringskode.NaringskodeHierarki
import no.nav.syfo.naringskode.NaringskodeStandard
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.generateJWT
import no.nav.syfo.testhelper.setupApiAndClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NaringskodeApiTest {
    private val externalMockEnvironment = ExternalMockEnvironment()
    private val ssbClient = externalMockEnvironment.ssbClient
    private val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
    )

    @Test
    fun `returns the full parent chain for the selected standard without personident`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        NaringskodeStandard.entries.forEach { standard ->
            val codes = when (standard) {
                NaringskodeStandard.SN1994, NaringskodeStandard.SN2002 ->
                    listOf("72.100", "72.10", "72.1", "72", "K_", "K")
                NaringskodeStandard.SN2007 -> listOf("62.020", "62.02", "62.0", "62", "J")
                NaringskodeStandard.SN2025 -> listOf("62.200", "62.20", "62.2", "62", "K")
            }
            val hierarchy = codes.mapIndexed { index, code ->
                Naringskode(code, "Name $code", codes.getOrNull(index + 1), shortName = "Short $code")
            }
            coEvery { ssbClient.getNaringskoder(standard) } returns hierarchy.reversed()

            val response = client.get("$apiNaringskodeBasePath/${codes.first()}?standard=${standard.year}") {
                bearerAuth(validToken)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(NaringskodeHierarki(standard.year, hierarchy), response.body<NaringskodeHierarki>())
            val rootResponse = client.get("$apiNaringskodeBasePath/${codes.last()}?standard=${standard.year}") {
                bearerAuth(validToken)
            }
            assertEquals(
                NaringskodeHierarki(standard.year, listOf(hierarchy.last())),
                rootResponse.body<NaringskodeHierarki>(),
            )
            if (standard == NaringskodeStandard.SN1994 || standard == NaringskodeStandard.SN2002) {
                val intermediateResponse = client.get("$apiNaringskodeBasePath/K_?standard=${standard.year}") {
                    bearerAuth(validToken)
                }
                assertEquals(HttpStatusCode.OK, intermediateResponse.status)
                assertEquals(
                    NaringskodeHierarki(standard.year, hierarchy.takeLast(2)),
                    intermediateResponse.body<NaringskodeHierarki>(),
                )
            }
        }
    }

    @Test
    fun `requires authentication`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)

        val response = client.get("$apiNaringskodeBasePath/62.200?standard=2025")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        coVerify(exactly = 0) { ssbClient.getNaringskoder(any()) }
    }

    @Test
    fun `rejects missing or unsupported standard and malformed codes before calling SSB`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        listOf(
            "62.200",
            "62.200?standard=1983",
            "62.200?standard=SN2025",
            "invalid?standard=2025",
            "62200?standard=2025",
        ).forEach { path ->
            val response = client.get("$apiNaringskodeBasePath/$path") {
                bearerAuth(validToken)
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
        coVerify(exactly = 0) { ssbClient.getNaringskoder(any()) }
    }

    @Test
    fun `returns not found when code is absent from the selected standard`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        coEvery { ssbClient.getNaringskoder(NaringskodeStandard.SN2007) } returns listOf(
            Naringskode("J", "Informasjon og kommunikasjon", null),
        )

        val response = client.get("$apiNaringskodeBasePath/62.200?standard=2007") {
            bearerAuth(validToken)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        coVerify(exactly = 1) { ssbClient.getNaringskoder(NaringskodeStandard.SN2007) }
        coVerify(exactly = 0) { ssbClient.getNaringskoder(NaringskodeStandard.SN2025) }
    }

    @Test
    fun `returns bad gateway when SSB fails`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        coEvery { ssbClient.getNaringskoder(any()) } throws SsbException("Unavailable", RuntimeException())

        val response = client.get("$apiNaringskodeBasePath/62.200?standard=2025") {
            bearerAuth(validToken)
        }

        assertEquals(HttpStatusCode.BadGateway, response.status)
    }
}
