package no.nav.syfo.sektorkode.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.syfo.client.ssb.SsbException
import no.nav.syfo.sektorkode.Sektorkode
import no.nav.syfo.sektorkode.SektorkodeHierarki
import no.nav.syfo.sektorkode.SektorkodeStandard
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.generateJWT
import no.nav.syfo.testhelper.setupApiAndClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SektorkodeApiTest {
    private val externalMockEnvironment = ExternalMockEnvironment()
    private val ssbClient = externalMockEnvironment.ssbClient
    private val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
    )

    @Test
    fun `returns the full parent chain and allows an intermediate code for both standards`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)

        SektorkodeStandard.entries.forEach { standard ->
            val rootCode = if (standard == SektorkodeStandard.IS1987) "ALLENO" else "INNL"
            val middleCode = if (standard == SektorkodeStandard.IS1987) "A_OFF" else "C_OFF"
            val leafCode = if (standard == SektorkodeStandard.IS1987) "110" else "6100"
            val hierarchy = listOf(
                Sektorkode(rootCode, "Root $rootCode", null, 1),
                Sektorkode(middleCode, "Middle $middleCode", rootCode, 2),
                Sektorkode(leafCode, "Leaf $leafCode", middleCode, 3),
            )
            val ekstraKode = if (standard == SektorkodeStandard.IS1987) {
                Sektorkode("C_IKKE-FIN", "Ikke-finansiell sektor", rootCode, 2)
            } else {
                null
            }
            coEvery { ssbClient.getSektorkoder(standard) } returns hierarchy + listOfNotNull(ekstraKode)

            val response = client.get("$apiSektorkodeBasePath/$leafCode?standard=${standard.year}") {
                bearerAuth(validToken)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                SektorkodeHierarki(standard.year, hierarchy.asReversed()),
                response.body<SektorkodeHierarki>(),
            )

            val middleResponse = client.get("$apiSektorkodeBasePath/$middleCode?standard=${standard.year}") {
                bearerAuth(validToken)
            }
            assertEquals(
                SektorkodeHierarki(standard.year, hierarchy.take(2).asReversed()),
                middleResponse.body<SektorkodeHierarki>(),
            )

            if (ekstraKode != null) {
                val hyphenatedResponse =
                    client.get("$apiSektorkodeBasePath/${ekstraKode.code}?standard=${standard.year}") {
                        bearerAuth(validToken)
                    }
                assertEquals(HttpStatusCode.OK, hyphenatedResponse.status)
                assertEquals(
                    SektorkodeHierarki(standard.year, listOf(ekstraKode, hierarchy.first())),
                    hyphenatedResponse.body<SektorkodeHierarki>(),
                )
            }
        }
    }

    @Test
    fun `requires authentication before fetching the hierarchy`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)

        val response = client.get("$apiSektorkodeBasePath/6100?standard=2012")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        coVerify(exactly = 0) { ssbClient.getSektorkoder(any()) }
    }

    @Test
    fun `rejects invalid requests before fetching SSB`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)

        listOf(
            "6100",
            "6100?standard=1983",
            "6100?standard=IS2012",
            "6100?standard=1987&standard=2012",
            "61000?standard=2012",
            "c_OFF?standard=2012",
        ).forEach { path ->
            val response = client.get("$apiSektorkodeBasePath/$path") {
                bearerAuth(validToken)
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
        coVerify(exactly = 0) { ssbClient.getSektorkoder(any()) }
    }

    @Test
    fun `returns not found when code is absent from the selected standard`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        coEvery { ssbClient.getSektorkoder(SektorkodeStandard.IS2012) } returns listOf(
            Sektorkode("INNL", "Innenlandske sektorer", null, 1),
        )

        val response = client.get("$apiSektorkodeBasePath/6100?standard=2012") {
            bearerAuth(validToken)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        coVerify(exactly = 1) { ssbClient.getSektorkoder(SektorkodeStandard.IS2012) }
        coVerify(exactly = 0) { ssbClient.getSektorkoder(SektorkodeStandard.IS1987) }
    }

    @Test
    fun `returns bad gateway when SSB fails`() = testApplication {
        val client = setupApiAndClient(externalMockEnvironment)
        coEvery { ssbClient.getSektorkoder(any()) } throws SsbException("Unavailable", RuntimeException())

        val response = client.get("$apiSektorkodeBasePath/6100?standard=2012") {
            bearerAuth(validToken)
        }

        assertEquals(HttpStatusCode.BadGateway, response.status)
    }
}
