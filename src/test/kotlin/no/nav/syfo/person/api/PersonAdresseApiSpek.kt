package no.nav.syfo.person.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.person.api.domain.PersonAdresseResponse
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.util.*
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PersonAdresseApiSpek : Spek({
    describe(PersonAdresseApiSpek::class.java.simpleName) {

        val externalMockEnvironment = ExternalMockEnvironment()

        beforeGroup {
            externalMockEnvironment.startExternalMocks()
        }

        afterGroup {
            externalMockEnvironment.stopExternalMocks()
        }

        val url = "$apiPersonBasePath$apiPersonAdressePath"

        describe("Get Adresse of person") {
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
                        val personAdresseResponse = response.body<PersonAdresseResponse>()
                        personAdresseResponse.navn shouldBeEqualTo generatePdlPersonResponse(ARBEIDSTAKER_PERSONIDENT).data?.hentPerson?.fullName
                        personAdresseResponse.bostedsadresse shouldBeEqualTo generatePersonAdresseResponse().bostedsadresse
                        personAdresseResponse.kontaktadresse shouldBeEqualTo generatePersonAdresseResponse().kontaktadresse
                        personAdresseResponse.oppholdsadresse shouldBeEqualTo generatePersonAdresseResponse().oppholdsadresse
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
