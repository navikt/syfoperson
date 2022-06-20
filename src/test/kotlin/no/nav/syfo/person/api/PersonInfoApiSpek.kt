package no.nav.syfo.person.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.server.testing.*
import no.nav.syfo.person.api.domain.*
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.util.*
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PersonInfoApiSpek : Spek({
    val objectMapper: ObjectMapper = configuredJacksonMapper()

    describe(PersonInfoApiSpek::class.java.simpleName) {

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

            val url = "$apiPersonBasePath$apiPersonInfoPath"

            describe("Get PersonInfo of list of persons") {
                val validToken = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                )

                describe("Happy path") {
                    it("should return OK if request is successful") {
                        val requestBody = listOf(
                            PersonInfoRequest(ARBEIDSTAKER_PERSONIDENT.value),
                            PersonInfoRequest(ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value),
                            PersonInfoRequest(ARBEIDSTAKER_ADRESSEBESKYTTET.value),
                            PersonInfoRequest(ARBEIDSTAKER_VEILEDER_NO_ACCESS.value),
                        )
                        with(
                            handleRequest(HttpMethod.Post, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                setBody(objectMapper.writeValueAsString(requestBody))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val personInfoList: List<PersonInfo> =
                                objectMapper.readValue(response.content!!)
                            personInfoList.size shouldBeEqualTo requestBody.size - 1
                            personInfoList[0].fnr shouldBeEqualTo ARBEIDSTAKER_PERSONIDENT.value
                            personInfoList[0].skjermingskode shouldBeEqualTo Skjermingskode.EGEN_ANSATT
                            personInfoList[1].fnr shouldBeEqualTo ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value
                            personInfoList[1].skjermingskode shouldBeEqualTo Skjermingskode.INGEN
                            personInfoList[2].fnr shouldBeEqualTo ARBEIDSTAKER_ADRESSEBESKYTTET.value
                            personInfoList[2].skjermingskode shouldBeEqualTo Skjermingskode.DISKRESJONSMERKET
                        }
                    }
                }
                describe("Unhappy paths") {
                    it("should return status ${HttpStatusCode.Unauthorized} if no token is supplied") {
                        with(
                            handleRequest(HttpMethod.Post, url) {}
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Unauthorized
                        }
                    }
                    it("should return status ${HttpStatusCode.InternalServerError} if not body is supplied") {
                        with(
                            handleRequest(HttpMethod.Post, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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
