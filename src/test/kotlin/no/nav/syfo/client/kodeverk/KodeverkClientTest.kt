package no.nav.syfo.client.kodeverk

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.azuread.AzureAdToken
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.startExternalMocks
import no.nav.syfo.testhelper.stopExternalMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class KodeverkClientTest {

    private val externalMockEnvironment = ExternalMockEnvironment()
    private val valkeyStore = mockk<ValkeyStore>(relaxed = true)
    private val azureAdClient = mockk<AzureAdClient>()
    private val kodeverkClient = KodeverkClient(
        azureAdClient = azureAdClient,
        valkeyStore = valkeyStore,
        baseUrl = externalMockEnvironment.environment.kodeverkUrl,
        clientId = externalMockEnvironment.environment.kodeverkClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    private val callId = "callId"

    @BeforeEach
    fun beforeEach() {
        coEvery {
            azureAdClient.getSystemToken(any())
        } returns AzureAdToken(
            accessToken = "token",
            expires = LocalDateTime.now().plusDays(1)
        )
        externalMockEnvironment.startExternalMocks()
        clearMocks(valkeyStore)
    }

    @AfterEach
    fun afterEach() {
        externalMockEnvironment.stopExternalMocks()
    }

    @Test
    fun `returns cached value`() {
        every { valkeyStore.getListObject<Postinformasjon>(any()) } returns postinformasjonList

        runBlocking {
            assertEquals(postinformasjonList, kodeverkClient.getPostinformasjon(callId))
        }

        verify(exactly = 1) { valkeyStore.get(key = "postinformasjon") }
        verify(exactly = 0) { valkeyStore.setObject<Postinformasjon>(any(), any(), any()) }
    }

    @Test
    fun `fetch postinformasjon from kodeverk and caches value when cache is empty`() {
        every { valkeyStore.getListObject<Postinformasjon>(any()) } returns null

        runBlocking {
            assertEquals(postinformasjonFromServerMock, kodeverkClient.getPostinformasjon(callId))
        }

        verify(exactly = 1) { valkeyStore.get(key = "postinformasjon") }
        verify(exactly = 1) {
            valkeyStore.setObject(
                expireSeconds = 86400,
                key = "postinformasjon",
                value = postinformasjonFromServerMock,
            )
        }
    }

    @Test
    fun `Don't store empty list in cache if call to Kodeverk fails`() {
        every { valkeyStore.getListObject<Postinformasjon>(any()) } returns null

        runBlocking {
            kodeverkClient.getPostinformasjon(callId = "500")
        }

        verify(exactly = 1) { valkeyStore.get(key = "postinformasjon") }
        verify(exactly = 0) { valkeyStore.setObject<Postinformasjon>(any(), any(), any()) }
    }
}

val postinformasjonList = listOf<Postinformasjon>(
    Postinformasjon(
        postnummer = "9990",
        poststed = "BÅTSFJORD",
    ),
)

val postinformasjonFromServerMock = listOf(
    Postinformasjon(
        postnummer = "1001",
        poststed = "OSLO",
    )
)
