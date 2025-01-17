package no.nav.syfo.person.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.person.api.domain.syfomodiaperson.SyfomodiapersonKontaktinfo
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PersonKontaktinformasjonApiSpek : Spek({
    describe(PersonKontaktinformasjonApiSpek::class.java.simpleName) {

        val externalMockEnvironment = ExternalMockEnvironment()

        beforeGroup {
            externalMockEnvironment.startExternalMocks()
        }

        afterGroup {
            externalMockEnvironment.stopExternalMocks()
        }

        val url = "$apiPersonBasePath$apiPersonKontaktinformasjonPath"

        describe("Get Kontaktinformasjon of person") {
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
                        val digitalKontaktinfo = response.body<SyfomodiapersonKontaktinfo>()
                        digitalKontaktinfo.fnr shouldBeEqualTo ARBEIDSTAKER_PERSONIDENT.value
                        digitalKontaktinfo.skalHaVarsel shouldBeEqualTo true
                        digitalKontaktinfo.tlf shouldBeEqualTo UserConstants.PERSON_TLF
                        digitalKontaktinfo.epost shouldBeEqualTo UserConstants.PERSON_EMAIL
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
