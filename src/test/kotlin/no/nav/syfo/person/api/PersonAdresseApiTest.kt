package no.nav.syfo.person.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.person.api.domain.PersonAdresseResponse
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class PersonAdresseApiTest {

    private val externalMockEnvironment = ExternalMockEnvironment()
    private val url = "$apiPersonBasePath$apiPersonAdressePath"
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
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val personAdresseResponse = response.body<PersonAdresseResponse>()
                assertEquals(
                    generatePdlPersonResponse(ARBEIDSTAKER_PERSONIDENT).data?.hentPerson?.fullName,
                    personAdresseResponse.navn
                )
                assertEquals(generatePersonAdresseResponse().bostedsadresse, personAdresseResponse.bostedsadresse)
                assertEquals(generatePersonAdresseResponse().kontaktadresse, personAdresseResponse.kontaktadresse)
                assertEquals(generatePersonAdresseResponse().oppholdsadresse, personAdresseResponse.oppholdsadresse)
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
                val response = client.get(url)

                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }
        }

        @Test
        fun `should return status 400 Bad Request if not PersonIdent is supplied`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                }

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }
        }

        @Test
        fun `should return status 403 Forbidden if access to PersonIdent is denied`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_VEILEDER_NO_ACCESS.value)
                }

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }
        }

        @Test
        fun `should return status 500 Internal Server Error if person is null from pdl`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PDL_ERROR.value)
                }

                assertEquals(HttpStatusCode.InternalServerError, response.status)
            }
        }
    }
}
