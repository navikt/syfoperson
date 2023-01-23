package no.nav.syfo.person.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.server.testing.*
import no.nav.syfo.client.pdl.getFullName
import no.nav.syfo.person.api.domain.syfomodiaperson.SyfomodiapersonBrukerinfo
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_DOED
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.testhelper.mock.digitalKontaktinfoBolkKanVarslesTrue
import no.nav.syfo.util.*
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
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
                            syfomodiapersonBrukerinfo.navn shouldBeEqualTo externalMockEnvironment.pdlMock.personResponseDefault.data?.getFullName()
                            syfomodiapersonBrukerinfo.doedsdato shouldBe null
                        }
                    }
                    it("should include doedsdato") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_DOED.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val syfomodiapersonBrukerinfo: SyfomodiapersonBrukerinfo =
                                objectMapper.readValue(response.content!!)
                            syfomodiapersonBrukerinfo.navn shouldBeEqualTo externalMockEnvironment.pdlMock.personResponseDefault.data?.getFullName()
                            syfomodiapersonBrukerinfo.doedsdato shouldBeEqualTo LocalDate.now()
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
                }
            }
        }
    }
})
