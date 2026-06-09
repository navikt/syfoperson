package no.nav.syfo.client.aareg

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AAREG_NOT_FOUND
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.mock.generateArbeidsforholdResponse
import no.nav.syfo.testhelper.mock.mockedAzureAdToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AaregClientTest {

    private val externalMockEnvironment = ExternalMockEnvironment()
    private val valkeyStore = mockk<ValkeyStore>(relaxed = true)
    private val azureAdClient = mockk<AzureAdClient>()
    private val aaregClient = AaregClient(
        azureAdClient = azureAdClient,
        valkeyStore = valkeyStore,
        baseUrl = externalMockEnvironment.environment.aaregUrl,
        clientId = externalMockEnvironment.environment.aaregClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    private val callId = "callId"
    private val token = "token"

    @BeforeEach
    fun beforeEach() {
        coEvery {
            azureAdClient.getOnBehalfOfToken(any(), any())
        } returns mockedAzureAdToken
        clearMocks(valkeyStore)
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `returns cached arbeidsforhold without calling Aareg`() {
        val cachedArbeidsforhold = listOf(generateArbeidsforholdResponse())
        every { valkeyStore.getListObject<ArbeidsforholdResponse>(any()) } returns cachedArbeidsforhold

        val result = runBlocking {
            aaregClient.getArbeidsforhold(ARBEIDSTAKER_PERSONIDENT, token, callId)
        }

        assertEquals(cachedArbeidsforhold, result)
        verify(exactly = 1) { valkeyStore.getListObject<ArbeidsforholdResponse>(any()) }
        verify(exactly = 0) { valkeyStore.setObject<List<ArbeidsforholdResponse>>(any(), any(), any()) }
        coVerify(exactly = 0) { azureAdClient.getOnBehalfOfToken(any(), any()) }
    }

    @Test
    fun `fetches arbeidsforhold from Aareg and caches result when cache is empty`() {
        every { valkeyStore.getListObject<ArbeidsforholdResponse>(any()) } returns null

        val result = runBlocking {
            aaregClient.getArbeidsforhold(ARBEIDSTAKER_PERSONIDENT, token, callId)
        }

        assertEquals(1, result.size)
        assertEquals("912345678", result.first().arbeidssted.identer.first().ident)
        verify(exactly = 1) { valkeyStore.getListObject<ArbeidsforholdResponse>(any()) }
        verify(exactly = 1) {
            valkeyStore.setObject(
                expireSeconds = 12 * 60 * 60L,
                key = "AAREG_ARBEIDSFORHOLD-${ARBEIDSTAKER_PERSONIDENT.value}",
                value = result,
            )
        }
    }

    @Test
    fun `returns empty list on 404 from Aareg and does not cache`() {
        every { valkeyStore.getListObject<ArbeidsforholdResponse>(any()) } returns null

        val result = runBlocking {
            aaregClient.getArbeidsforhold(ARBEIDSTAKER_AAREG_NOT_FOUND, token, callId)
        }

        assertTrue(result.isEmpty())
        verify(exactly = 1) { valkeyStore.getListObject<ArbeidsforholdResponse>(any()) }
        verify(exactly = 0) { valkeyStore.setObject<List<ArbeidsforholdResponse>>(any(), any(), any()) }
    }

    @Test
    fun `throws exception and does not cache when Azure OBO token fails`() {
        every { valkeyStore.getListObject<ArbeidsforholdResponse>(any()) } returns null
        coEvery {
            azureAdClient.getOnBehalfOfToken(any(), any())
        } returns null

        assertThrows<RuntimeException> {
            runBlocking {
                aaregClient.getArbeidsforhold(ARBEIDSTAKER_PERSONIDENT, token, callId)
            }
        }

        verify(exactly = 1) { valkeyStore.getListObject<ArbeidsforholdResponse>(any()) }
        verify(exactly = 0) { valkeyStore.setObject<List<ArbeidsforholdResponse>>(any(), any(), any()) }
    }
}
