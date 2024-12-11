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
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PersonInfoApiSpek : Spek({
    describe(PersonInfoApiSpek::class.java.simpleName) {

        val externalMockEnvironment = ExternalMockEnvironment()

        beforeGroup {
            externalMockEnvironment.startExternalMocks()
        }

        afterGroup {
            externalMockEnvironment.stopExternalMocks()
        }

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

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val personInfoList = response.body<List<PersonInfo>>()
                        personInfoList.size shouldBeEqualTo requestBody.size - 2 // Should not contain info for ARBEIDSTAKER_VEILEDER_NO_ACCESS and ARBEIDSTAKER_PDL_ERROR
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
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.post(url)

                        response.status shouldBeEqualTo HttpStatusCode.Unauthorized
                    }
                }
                it("should return status ${HttpStatusCode.InternalServerError} if not body is supplied") {
                    testApplication {
                        val client = setupApiAndClient(externalMockEnvironment)
                        val response = client.post(url) {
                            bearerAuth(validToken)
                            contentType(ContentType.Application.Json)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.InternalServerError
                    }
                }
            }
        }
    }
})
