package no.nav.syfo.person.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.server.testing.*
import no.nav.syfo.person.api.domain.syfomodiaperson.SyfomodiapersonBrukerinfo
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_DOD
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PDL_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_SIKKERHETSTILTAK
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.testhelper.mock.digitalKontaktinfoBolkKanVarslesTrue
import no.nav.syfo.util.*
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEmpty
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate

class PersonBrukerinfoApiSpek : Spek({
    val objectMapper: ObjectMapper = configuredJacksonMapper()

    describe(PersonBrukerinfoApiSpek::class.java.simpleName) {

        with(TestApplicationEngine()) {
            start()

            val externalMockEnvironment = ExternalMockEnvironment()

            beforeGroup {
                externalMockEnvironment.startExternalMocks()
            }

            afterGroup {
                externalMockEnvironment.stopExternalMocks()
            }

            application.testApiModule(
                externalMockEnvironment = externalMockEnvironment,
            )

            val url = "$apiPersonBasePath$apiPersonBrukerinfoPath"

            describe("Get Brukerinfo of person") {
                val validToken = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                )

                describe("Happy path") {
                    it("should return OK if request is successful") {
                        val digitalKontaktinfoBolkKanVarslesTrue = digitalKontaktinfoBolkKanVarslesTrue(
                            personIdentNumber = ARBEIDSTAKER_PERSONIDENT.value,
                        ).personer?.get(ARBEIDSTAKER_PERSONIDENT.value)!!

                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val syfomodiapersonBrukerinfo: SyfomodiapersonBrukerinfo =
                                objectMapper.readValue(response.content!!)
                            syfomodiapersonBrukerinfo.kontaktinfo.fnr shouldBeEqualTo ARBEIDSTAKER_PERSONIDENT.value
                            syfomodiapersonBrukerinfo.kontaktinfo.epost shouldBeEqualTo digitalKontaktinfoBolkKanVarslesTrue.epostadresse
                            syfomodiapersonBrukerinfo.kontaktinfo.tlf shouldBeEqualTo digitalKontaktinfoBolkKanVarslesTrue.mobiltelefonnummer
                            syfomodiapersonBrukerinfo.kontaktinfo.skalHaVarsel shouldBeEqualTo true
                            syfomodiapersonBrukerinfo.navn shouldBeEqualTo externalMockEnvironment.pdlMock.personResponseDefault.data?.hentPerson?.fullName
                            syfomodiapersonBrukerinfo.dodsdato shouldBe null
                            syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon shouldBe null
                        }
                    }
                    it("should include dodsdato") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_DOD.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val syfomodiapersonBrukerinfo: SyfomodiapersonBrukerinfo =
                                objectMapper.readValue(response.content!!)
                            syfomodiapersonBrukerinfo.navn shouldBeEqualTo externalMockEnvironment.pdlMock.personResponseDefault.data?.hentPerson?.fullName
                            syfomodiapersonBrukerinfo.dodsdato shouldBeEqualTo LocalDate.now()
                        }
                    }
                    it("should include tilrettelagtKommunikasjon") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val syfomodiapersonBrukerinfo: SyfomodiapersonBrukerinfo =
                                objectMapper.readValue(response.content!!)
                            syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon?.talesprakTolk?.value shouldBeEqualTo "NO"
                            syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon?.tegnsprakTolk?.value shouldBeEqualTo null
                        }
                    }
                    it("includes sikkerhetstiltak") {
                        val expectedSikkerhetstiltak = generatePdlSikkerhetsiltak()

                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_SIKKERHETSTILTAK.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val brukerinfo: SyfomodiapersonBrukerinfo = objectMapper.readValue(response.content!!)
                            brukerinfo.sikkerhetstiltak.shouldNotBeEmpty()

                            val sikkerhetstiltak = brukerinfo.sikkerhetstiltak.first()
                            sikkerhetstiltak.type.shouldBeEqualTo(expectedSikkerhetstiltak.tiltakstype.name)
                            sikkerhetstiltak.beskrivelse.shouldBeEqualTo(expectedSikkerhetstiltak.beskrivelse)
                            sikkerhetstiltak.gyldigFom.shouldBeEqualTo(expectedSikkerhetstiltak.gyldigFraOgMed)
                            sikkerhetstiltak.gyldigTom.shouldBeEqualTo(expectedSikkerhetstiltak.gyldigTilOgMed)
                        }
                    }
                }
                describe("Unhappy paths") {
                    it("should return status ${HttpStatusCode.Unauthorized} if no token is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, url) {}
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Unauthorized
                        }
                    }
                    it("should return status ${HttpStatusCode.BadRequest} if not PersonIdent is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                        }
                    }
                    it("should return status ${HttpStatusCode.Forbidden} if access to PersonIdent is denied") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_VEILEDER_NO_ACCESS.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Forbidden
                        }
                    }
                    it("should return status ${HttpStatusCode.InternalServerError} if person is null from pdl") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PDL_ERROR.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.InternalServerError
                        }
                    }
                }
            }
        }
    }
})
