package no.nav.syfo.person.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.person.api.domain.syfomodiaperson.SyfomodiapersonBrukerinfo
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_DOD
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT_CHANGED
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_SIKKERHETSTILTAK
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEmpty
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate

class PersonBrukerinfoApiSpek : Spek({
    describe(PersonBrukerinfoApiSpek::class.java.simpleName) {
        val externalMockEnvironment = ExternalMockEnvironment()

        beforeGroup {
            externalMockEnvironment.startExternalMocks()
        }

        afterGroup {
            externalMockEnvironment.stopExternalMocks()
        }

        val url = "$apiPersonBasePath$apiPersonBrukerinfoPath"

        describe("Get Brukerinfo of person") {
            val validToken = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
            )

            describe("Happy path") {
                it("should return OK if request is successful") {
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.get(url) {
                            bearerAuth(validToken)
                            header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val syfomodiapersonBrukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                        syfomodiapersonBrukerinfo.aktivPersonident shouldBeEqualTo ARBEIDSTAKER_PERSONIDENT.value
                        syfomodiapersonBrukerinfo.navn shouldBeEqualTo generatePdlPersonResponse().data?.hentPerson?.fullName
                        syfomodiapersonBrukerinfo.dodsdato shouldBe null
                        syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon shouldBe null
                    }
                }
                it("should include aktiv ident") {
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.get(url) {
                            bearerAuth(validToken)
                            header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT_CHANGED.value)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val syfomodiapersonBrukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                        syfomodiapersonBrukerinfo.aktivPersonident shouldBeEqualTo ARBEIDSTAKER_PERSONIDENT.value
                        syfomodiapersonBrukerinfo.navn shouldBeEqualTo generatePdlPersonResponse().data?.hentPerson?.fullName
                        syfomodiapersonBrukerinfo.dodsdato shouldBe null
                        syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon shouldBe null
                    }
                }
                it("should include dodsdato") {
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.get(url) {
                            bearerAuth(validToken)
                            header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_DOD.value)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val syfomodiapersonBrukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                        syfomodiapersonBrukerinfo.navn shouldBeEqualTo generatePdlPersonResponse().data?.hentPerson?.fullName
                        syfomodiapersonBrukerinfo.dodsdato shouldBeEqualTo LocalDate.now()
                    }
                }
                it("should include tilrettelagtKommunikasjon") {
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.get(url) {
                            bearerAuth(validToken)
                            header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON.value)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val syfomodiapersonBrukerinfo = response.body<SyfomodiapersonBrukerinfo>()
                        syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon?.talesprakTolk?.value shouldBeEqualTo "NO"
                        syfomodiapersonBrukerinfo.tilrettelagtKommunikasjon?.tegnsprakTolk?.value shouldBeEqualTo null
                    }
                }
                it("includes sikkerhetstiltak") {
                    val expectedSikkerhetstiltak = generatePdlSikkerhetsiltak()

                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.get(url) {
                            bearerAuth(validToken)
                            header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_SIKKERHETSTILTAK.value)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val brukerinfo = response.body<SyfomodiapersonBrukerinfo>()
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
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.get(url)

                        response.status shouldBeEqualTo HttpStatusCode.Unauthorized
                    }
                }
                it("should return status ${HttpStatusCode.BadRequest} if not PersonIdent is supplied") {
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.get(url) {
                            bearerAuth(validToken)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.BadRequest
                    }
                }
                it("should return status ${HttpStatusCode.Forbidden} if access to PersonIdent is denied") {
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.get(url) {
                            bearerAuth(validToken)
                            header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_VEILEDER_NO_ACCESS.value)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.Forbidden
                    }
                }
                it("should return status ${HttpStatusCode.InternalServerError} if person is null from pdl") {
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.get(url) {
                            bearerAuth(validToken)
                            header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PDL_ERROR.value)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.InternalServerError
                    }
                }
            }
        }
    }
})
