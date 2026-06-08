package no.nav.syfo.person.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.every
import no.nav.syfo.client.aareg.ArbeidsforholdResponse
import no.nav.syfo.client.azuread.AzureAdToken
import no.nav.syfo.person.api.domain.ArbeidsforholdPersonDTO
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AAREG_SEVERAL_ARBEIDSFORHOLD
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.testhelper.generateJWT
import no.nav.syfo.testhelper.setupApiAndClient
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PersonArbeidsforholdApiTest {

    private val externalMockEnvironment = ExternalMockEnvironment()
    private val url = "$apiPersonBasePath/arbeidsforhold"
    private val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
    )
    private val valkeyMock = externalMockEnvironment.valkeyStore

    @BeforeEach
    fun beforeEach() {
        every { valkeyMock.getObject<AzureAdToken?>(key = any()) } returns null
        every { valkeyMock.getListObject<ArbeidsforholdResponse?>(key = any()) } returns null
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
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
                val arbeidsforholdPerson = response.body<ArbeidsforholdPersonDTO>()
                val arbeidsforhold = arbeidsforholdPerson.arbeidsforhold

                assertEquals(ARBEIDSTAKER_PERSONIDENT.value, arbeidsforholdPerson.personident)
                assertEquals(1, arbeidsforhold.size)
                assertEquals("912345678", arbeidsforhold.first().orgnummer)
                assertEquals("Utvikler", arbeidsforhold.first().yrke)
                assertEquals("100", arbeidsforhold.first().stillingsprosent)
                assertEquals("Fast ansettelse", arbeidsforhold.first().ansettelsesform)
            }
        }

        @Test
        fun `should handle several yrker for same orgnummer`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val arbeidsforholdPerson = response.body<ArbeidsforholdPersonDTO>()
                val arbeidsforhold = arbeidsforholdPerson.arbeidsforhold

                assertEquals(ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value, arbeidsforholdPerson.personident)
                assertEquals(1, arbeidsforhold.size)
                assertEquals("912345699", arbeidsforhold.first().orgnummer)
                assertEquals("AKS-lærer", arbeidsforhold.first().yrke)
                assertEquals("100", arbeidsforhold.first().stillingsprosent)
                assertEquals("Fast ansettelse", arbeidsforhold.first().ansettelsesform)
            }
        }

        @Test
        fun `should handle several arbeidsforhold`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_AAREG_SEVERAL_ARBEIDSFORHOLD.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val arbeidsforholdPerson = response.body<ArbeidsforholdPersonDTO>()
                val arbeidsforhold = arbeidsforholdPerson.arbeidsforhold

                assertEquals(ARBEIDSTAKER_AAREG_SEVERAL_ARBEIDSFORHOLD.value, arbeidsforholdPerson.personident)
                assertEquals(2, arbeidsforhold.size)
                assertEquals("912345678", arbeidsforhold.first().orgnummer)
                assertEquals("912345699", arbeidsforhold.last().orgnummer)
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
        fun `should return status 400 Bad Request if no Personident is supplied`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                }

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }
        }

        @Test
        fun `should return status 403 Forbidden if access to Personident is denied`() {
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
