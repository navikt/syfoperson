package no.nav.syfo.client.aap

import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.mock.AAP_EMPTY_PERSONIDENT
import no.nav.syfo.testhelper.mock.AAP_ERROR_PERSONIDENT
import no.nav.syfo.testhelper.mock.mockedAzureAdToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AapClientTest {
    private val externalMockEnvironment = ExternalMockEnvironment()
    private val valkeyStore = mockk<ValkeyStore>(relaxed = true)
    private val azureAdClient = mockk<AzureAdClient>()
    private val client = AapClient(
        azureAdClient = azureAdClient,
        valkeyStore = valkeyStore,
        baseUrl = externalMockEnvironment.environment.aapApiInternUrl,
        clientId = externalMockEnvironment.environment.aapApiInternClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    @BeforeEach
    fun beforeEach() {
        coEvery { azureAdClient.getOnBehalfOfToken(any(), any()) } returns mockedAzureAdToken
        clearMocks(valkeyStore)
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `returns cached response without requesting token`() {
        val cachedResponse = AapSakerResponse(emptyList())
        every { valkeyStore.getObject<AapSakerResponse>(any()) } returns cachedResponse

        val response = runBlocking {
            client.getSaker(ARBEIDSTAKER_PERSONIDENT, "token", "callId")
        }

        assertEquals(cachedResponse, response)
        coVerify(exactly = 0) { azureAdClient.getOnBehalfOfToken(any(), any()) }
        verify(exactly = 0) { valkeyStore.setObject<AapSakerResponse>(any(), any(), any()) }
    }

    @Test
    fun `fetches and caches successful response for twelve hours`() {
        every { valkeyStore.getObject<AapSakerResponse>(any()) } returns null

        val response = runBlocking {
            client.getSaker(ARBEIDSTAKER_PERSONIDENT, "token", "callId")
        }

        assertEquals(1, response.saker.size)
        verify(exactly = 1) {
            valkeyStore.setObject(
                expireSeconds = AapClient.CACHE_EXPIRE_SECONDS,
                key = "${AapClient.CACHE_KEY_PREFIX}-${ARBEIDSTAKER_PERSONIDENT.value}",
                value = response,
            )
        }
    }

    @Test
    fun `caches successful empty response`() {
        every { valkeyStore.getObject<AapSakerResponse>(any()) } returns null
        val personident = PersonIdentNumber(AAP_EMPTY_PERSONIDENT)

        val response = runBlocking {
            client.getSaker(personident, "token", "callId")
        }

        assertEquals(emptyList<AapSak>(), response.saker)
        verify(exactly = 1) {
            valkeyStore.setObject(
                expireSeconds = AapClient.CACHE_EXPIRE_SECONDS,
                key = "${AapClient.CACHE_KEY_PREFIX}-${personident.value}",
                value = response,
            )
        }
    }

    @Test
    fun `does not cache when token exchange fails`() {
        every { valkeyStore.getObject<AapSakerResponse>(any()) } returns null
        coEvery { azureAdClient.getOnBehalfOfToken(any(), any()) } returns null

        assertThrows<RuntimeException> {
            runBlocking {
                client.getSaker(
                    PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value),
                    "token",
                    "callId",
                )
            }
        }

        verify(exactly = 0) { valkeyStore.setObject<AapSakerResponse>(any(), any(), any()) }
    }

    @Test
    fun `does not cache failed upstream response`() {
        every { valkeyStore.getObject<AapSakerResponse>(any()) } returns null

        assertThrows<io.ktor.client.plugins.ServerResponseException> {
            runBlocking {
                client.getSaker(
                    PersonIdentNumber(AAP_ERROR_PERSONIDENT),
                    "token",
                    "callId",
                )
            }
        }

        verify(exactly = 0) { valkeyStore.setObject<AapSakerResponse>(any(), any(), any()) }
    }
}
