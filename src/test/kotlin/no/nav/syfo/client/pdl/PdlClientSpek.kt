package no.nav.syfo.client.pdl

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.azuread.AzureAdToken
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PDL_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.startExternalMocks
import no.nav.syfo.testhelper.stopExternalMocks
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime

class PdlClientSpek : Spek({

    val externalMockEnvironment = ExternalMockEnvironment()
    val cacheMock = mockk<ValkeyStore>(relaxed = true)
    val azureAdClient = mockk<AzureAdClient>()
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        valkeyStore = cacheMock,
        baseUrl = externalMockEnvironment.environment.pdlUrl,
        clientId = externalMockEnvironment.environment.pdlClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    coEvery {
        azureAdClient.getSystemToken(any())
    } returns AzureAdToken(
        accessToken = "token",
        expires = LocalDateTime.now().plusDays(1)
    )

    beforeEachTest {
        clearMocks(cacheMock)
    }

    beforeGroup {
        externalMockEnvironment.startExternalMocks()
    }

    afterGroup {
        externalMockEnvironment.stopExternalMocks()
    }

    describe("${PdlClient::class.java.simpleName} hasAdressebeskyttelse") {
        it("hasAdressebeskyttelse returns true when cached value is true") {
            every { cacheMock.getObject<Boolean>(any()) } returns true

            runBlocking {
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_ADRESSEBESKYTTET,
                    callId = "callId",
                ) shouldBeEqualTo true
            }
            verify(exactly = 1) { cacheMock.getObject<Boolean>(any()) }
            verify(exactly = 0) { cacheMock.setObject<Boolean>(any(), any(), any()) }
        }

        it("hasAdressebeskyttelse returns false when cached value is false") {
            every { cacheMock.getObject<Boolean>(any()) } returns false

            runBlocking {
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    callId = "callId",
                ) shouldBeEqualTo false
            }
            verify(exactly = 1) { cacheMock.getObject<Boolean>(any()) }
            verify(exactly = 0) { cacheMock.setObject<Boolean>(any(), any(), any()) }
        }

        it("hasAdressebeskyttelse returns false and caches value when no cached value and arbeidstaker ikke adressebeskyttet") {
            every { cacheMock.getObject<Boolean>(any()) } returns null
            justRun { cacheMock.setObject<Boolean>(any(), any(), any()) }

            runBlocking {
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    callId = "callId",
                ) shouldBeEqualTo false
            }
            verify(exactly = 1) { cacheMock.getObject<Boolean>(any()) }
            verify(exactly = 1) { cacheMock.setObject<Boolean>(any(), any(), any()) }
        }

        it("hasAdressebeskyttelse returns true and caches value when no cached value and arbeidstaker adressebeskyttet") {
            every { cacheMock.getObject<Boolean>(any()) } returns null
            justRun { cacheMock.setObject<Boolean>(any(), any(), any()) }

            runBlocking {
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_ADRESSEBESKYTTET,
                    callId = "callId",
                ) shouldBeEqualTo true
            }
            verify(exactly = 1) { cacheMock.getObject<Boolean>(any()) }
            verify(exactly = 1) { cacheMock.setObject<Boolean>(any(), any(), any()) }
        }

        it("hasAdressebeskyttelse returns null and doesnt cache when arbeidstaker returns error from pdl") {
            every { cacheMock.getObject<Boolean>(any()) } returns null
            justRun { cacheMock.setObject<Boolean>(any(), any(), any()) }

            runBlocking {
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_PDL_ERROR,
                    callId = "callId",
                ).shouldBeNull()
            }
            verify(exactly = 1) { cacheMock.getObject<Boolean>(any()) }
            verify(exactly = 0) { cacheMock.setObject<Boolean>(any(), any(), any()) }
        }
    }
})
