package no.nav.syfo.person.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.server.testing.*
import no.nav.syfo.person.api.domain.OppfolgingstilfelleDTO
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER_DEFAULT
import no.nav.syfo.testhelper.mock.kOppfolgingstilfelleDTO
import no.nav.syfo.util.*
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PersonOppfolgingstilfelleArbeidsgiverApiSpek : Spek({
    val objectMapper: ObjectMapper = configuredJacksonMapper()

    describe(PersonOppfolgingstilfelleArbeidsgiverApiSpek::class.java.simpleName) {

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

            val url =
                "$apiPersonBasePath$apiPersonOppfolgingstilfelleArbeidsgiverPath/${VIRKSOMHETSNUMMER_DEFAULT.value}"

            describe("Get Oppfolgingstifelle with no Arbeidsgiver of person") {
                val validToken = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                )

                describe("Happy path") {
                    it("should return OK if request is successful") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val oppfolgingstilfelleDTOList: List<OppfolgingstilfelleDTO> =
                                objectMapper.readValue(response.content!!)
                            oppfolgingstilfelleDTOList.size shouldBeEqualTo 1
                            oppfolgingstilfelleDTOList.first().fom shouldBeEqualTo kOppfolgingstilfelleDTO().tidslinje.first().dag
                            oppfolgingstilfelleDTOList.first().tom shouldBeEqualTo kOppfolgingstilfelleDTO().tidslinje.last().dag
                            oppfolgingstilfelleDTOList.first().virksomhetsnummer shouldBeEqualTo VIRKSOMHETSNUMMER_DEFAULT.value
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
