package no.nav.syfo.person.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.person.api.domain.PersonInfo
import no.nav.syfo.person.api.domain.PersonInfoRequest
import no.nav.syfo.person.api.domain.Skjermingskode
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PDL_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PersonInfoApiTest {

    private val externalMockEnvironment = ExternalMockEnvironment()
    private val url = "$apiPersonBasePath$apiPersonInfoPath"
    private val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
    )

    @BeforeEach
    fun beforeEach() {
        externalMockEnvironment.startExternalMocks()
    }

    @AfterEach
    fun afterEach() {
        externalMockEnvironment.stopExternalMocks()
    }

    @Nested
    @DisplayName("Happy path")
    inner class HappyPath {

        @Test
        fun `should return OK if request is successful`() {
            val requestBody = listOf(
                PersonInfoRequest(ARBEIDSTAKER_PERSONIDENT.value),
                PersonInfoRequest(ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value),
                PersonInfoRequest(ARBEIDSTAKER_ADRESSEBESKYTTET.value),
                PersonInfoRequest(ARBEIDSTAKER_VEILEDER_NO_ACCESS.value),
                PersonInfoRequest(ARBEIDSTAKER_PDL_ERROR.value)
            )
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.post(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val personInfoList = response.body<List<PersonInfo>>()
                assertEquals(
                    requestBody.size - 2,
                    personInfoList.size
                ) // Should not contain info for ARBEIDSTAKER_VEILEDER_NO_ACCESS and ARBEIDSTAKER_PDL_ERROR
                assertEquals(ARBEIDSTAKER_PERSONIDENT.value, personInfoList[0].fnr)
                assertEquals(Skjermingskode.EGEN_ANSATT, personInfoList[0].skjermingskode)
                assertEquals(ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value, personInfoList[1].fnr)
                assertEquals(Skjermingskode.INGEN, personInfoList[1].skjermingskode)
                assertEquals(ARBEIDSTAKER_ADRESSEBESKYTTET.value, personInfoList[2].fnr)
                assertEquals(Skjermingskode.DISKRESJONSMERKET, personInfoList[2].skjermingskode)
            }
        }
    }

    @Nested
    @DisplayName("Unhappy paths")
    inner class UnhappyPaths {

        @Test
        fun `should return status 401 Unauthorized if no token is supplied`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.post(url)

                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }
        }

        @Test
        fun `should return status 500 Internal Server Error if not body is supplied`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.post(url) {
                    bearerAuth(validToken)
                    contentType(ContentType.Application.Json)
                }

                assertEquals(HttpStatusCode.InternalServerError, response.status)
            }
        }
    }
}
