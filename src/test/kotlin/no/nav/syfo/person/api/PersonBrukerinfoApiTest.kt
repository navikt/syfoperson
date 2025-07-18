package no.nav.syfo.person.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.person.api.domain.syfomodiaperson.SyfomodiapersonBrukerinfo
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_DOD
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT_CHANGED
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_SIKKERHETSTILTAK
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import java.time.LocalDate

class PersonBrukerinfoApiTest {

    private val externalMockEnvironment = ExternalMockEnvironment()

    @BeforeEach
    fun beforeEach() {
        externalMockEnvironment.startExternalMocks()
    }

    @AfterEach
    fun afterEach() {
        externalMockEnvironment.stopExternalMocks()
    }

    private val url = "$apiPersonBasePath$apiPersonBrukerinfoPath"
    private val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
    )

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
                val syfomodiapersonBrukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                assertEquals(ARBEIDSTAKER_PERSONIDENT.value, syfomodiapersonBrukerinfo.aktivPersonident)
                assertEquals(
                    generatePdlPersonResponse(ARBEIDSTAKER_PERSONIDENT).data?.hentPerson?.fullName,
                    syfomodiapersonBrukerinfo.navn
                )
                assertNull(syfomodiapersonBrukerinfo.dodsdato)
                assertNotNull(syfomodiapersonBrukerinfo.fodselsdato)
                assertEquals(30, syfomodiapersonBrukerinfo.alder)
                assertEquals("KVINNE", syfomodiapersonBrukerinfo.kjonn)
                assertNull(syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon)
            }
        }

        @Test
        fun `should return OK and correct age if birthday tomorrow`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val syfomodiapersonBrukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                assertEquals(ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value, syfomodiapersonBrukerinfo.aktivPersonident)
                assertEquals(
                    generatePdlPersonResponse(ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT).data?.hentPerson?.fullName,
                    syfomodiapersonBrukerinfo.navn
                )
                assertNull(syfomodiapersonBrukerinfo.dodsdato)
                assertNotNull(syfomodiapersonBrukerinfo.fodselsdato)
                assertEquals(29, syfomodiapersonBrukerinfo.alder)
                assertEquals("KVINNE", syfomodiapersonBrukerinfo.kjonn)
                assertNull(syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon)
            }
        }

        @Test
        fun `should include aktiv ident`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT_CHANGED.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val syfomodiapersonBrukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                assertEquals(ARBEIDSTAKER_PERSONIDENT.value, syfomodiapersonBrukerinfo.aktivPersonident)
                assertEquals(
                    generatePdlPersonResponse(ARBEIDSTAKER_PERSONIDENT).data?.hentPerson?.fullName,
                    syfomodiapersonBrukerinfo.navn
                )
                assertNull(syfomodiapersonBrukerinfo.dodsdato)
                assertNull(syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon)
            }
        }

        @Test
        fun `should include dodsdato`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_DOD.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val syfomodiapersonBrukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                assertEquals(
                    generatePdlPersonResponse(ARBEIDSTAKER_DOD).data?.hentPerson?.fullName,
                    syfomodiapersonBrukerinfo.navn
                )
                assertEquals(LocalDate.now(), syfomodiapersonBrukerinfo.dodsdato)
            }
        }

        @Test
        fun `should include tilrettelagtKommunikasjon`() {
            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val syfomodiapersonBrukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                assertEquals("NO", syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon?.talesprakTolk?.value)
                assertNull(syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon?.tegnsprakTolk?.value)
            }
        }

        @Test
        fun `includes sikkerhetstiltak`() {
            val expectedSikkerhetstiltak = generatePdlSikkerhetsiltak()

            testApplication {
                val client = setupApiAndClient(externalMockEnvironment)
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_SIKKERHETSTILTAK.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val brukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                assertTrue(brukerinfo.sikkerhetstiltak.isNotEmpty())

                val sikkerhetstiltak = brukerinfo.sikkerhetstiltak.first()
                assertEquals(expectedSikkerhetstiltak.tiltakstype.name, sikkerhetstiltak.type)
                assertEquals(expectedSikkerhetstiltak.beskrivelse, sikkerhetstiltak.beskrivelse)
                assertEquals(expectedSikkerhetstiltak.gyldigFraOgMed, sikkerhetstiltak.gyldigFom)
                assertEquals(expectedSikkerhetstiltak.gyldigTilOgMed, sikkerhetstiltak.gyldigTom)
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
