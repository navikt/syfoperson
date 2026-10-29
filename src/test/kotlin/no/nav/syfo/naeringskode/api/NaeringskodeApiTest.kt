package no.nav.syfo.naeringskode.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.syfo.client.ssb.SsbException
import no.nav.syfo.naeringskode.Naeringskode
import no.nav.syfo.naeringskode.NaeringskodeHierarki
import no.nav.syfo.naeringskode.NaeringskodeStandard
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.generateJWT
import no.nav.syfo.testhelper.setupApiAndClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NaeringskodeApiTest {
    private val externalMockEnvironment = ExternalMockEnvironment()
    private val ssbClient = externalMockEnvironment.ssbClient
    private val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
    )

    @Test
    fun `returns the full parent chain for the selected standard without personident`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        NaeringskodeStandard.entries.forEach { standard ->
            val codes = if (standard == NaeringskodeStandard.SN2025) {
                listOf("62.200", "62.20", "62.2", "62", "K")
            } else {
                listOf("62.020", "62.02", "62.0", "62", "J")
            }
            val hierarchy = codes.mapIndexed { index, code ->
                Naeringskode(code, "Name $code", codes.getOrNull(index + 1), shortName = "Short $code")
            }
            coEvery { ssbClient.getNaeringskoder(standard) } returns hierarchy.reversed()

            val response = client.get("$apiNaeringskodeBasePath/${codes.first()}?standard=${standard.year}") {
                bearerAuth(validToken)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(NaeringskodeHierarki(standard.year, hierarchy), response.body<NaeringskodeHierarki>())
            val rootResponse = client.get("$apiNaeringskodeBasePath/${codes.last()}?standard=${standard.year}") {
                bearerAuth(validToken)
            }
            assertEquals(
                NaeringskodeHierarki(standard.year, listOf(hierarchy.last())),
                rootResponse.body<NaeringskodeHierarki>(),
            )
        }
    }

    @Test
    fun `requires authentication`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)

        val response = client.get("$apiNaeringskodeBasePath/62.200?standard=2025")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        coVerify(exactly = 0) { ssbClient.getNaeringskoder(any()) }
    }

    @Test
    fun `rejects missing or unsupported standard and malformed codes before calling SSB`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        listOf(
            "62.200",
            "62.200?standard=2002",
            "62.200?standard=SN2025",
            "invalid?standard=2025",
            "62200?standard=2025",
        ).forEach { path ->
            val response = client.get("$apiNaeringskodeBasePath/$path") {
                bearerAuth(validToken)
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
        coVerify(exactly = 0) { ssbClient.getNaeringskoder(any()) }
    }

    @Test
    fun `returns not found when code is absent from the selected standard`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        coEvery { ssbClient.getNaeringskoder(NaeringskodeStandard.SN2007) } returns listOf(
            Naeringskode("J", "Informasjon og kommunikasjon", null),
        )

        val response = client.get("$apiNaeringskodeBasePath/62.200?standard=2007") {
            bearerAuth(validToken)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        coVerify(exactly = 1) { ssbClient.getNaeringskoder(NaeringskodeStandard.SN2007) }
        coVerify(exactly = 0) { ssbClient.getNaeringskoder(NaeringskodeStandard.SN2025) }
    }

    @Test
    fun `returns bad gateway when SSB fails`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        coEvery { ssbClient.getNaeringskoder(any()) } throws SsbException("Unavailable", RuntimeException())

        val response = client.get("$apiNaeringskodeBasePath/62.200?standard=2025") {
            bearerAuth(validToken)
        }

        assertEquals(HttpStatusCode.BadGateway, response.status)
    }
}
