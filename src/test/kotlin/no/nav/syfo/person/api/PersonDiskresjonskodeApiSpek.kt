package no.nav.syfo.person.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PersonDiskresjonskodeApiSpek : Spek({
    describe(PersonDiskresjonskodeApiSpek::class.java.simpleName) {

        val externalMockEnvironment = ExternalMockEnvironment()

        beforeGroup {
            externalMockEnvironment.startExternalMocks()
        }

        afterGroup {
            externalMockEnvironment.stopExternalMocks()
        }

        val url = "$apiPersonBasePath$apiPersonDiskresjonskodePath"

        describe("Get Diskresjonskode of person") {
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
                            header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET.value)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val diskresjonskode = response.body<String>()
                        diskresjonskode shouldBeEqualTo "6"
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
            }
        }
    }
})
