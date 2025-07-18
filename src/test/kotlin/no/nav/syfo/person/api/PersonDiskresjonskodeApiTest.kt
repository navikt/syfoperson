package no.nav.syfo.person.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

class PersonDiskresjonskodeApiTest {

    private val externalMockEnvironment = ExternalMockEnvironment()
    private val url = "$apiPersonBasePath$apiPersonDiskresjonskodePath"
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
                    header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val diskresjonskode = response.body<String>()
                assertEquals("6", diskresjonskode)
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
    }
}
