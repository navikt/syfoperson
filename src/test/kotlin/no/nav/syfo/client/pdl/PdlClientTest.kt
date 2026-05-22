package no.nav.syfo.client.pdl

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PDL_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.generatePdlHentPerson
import no.nav.syfo.testhelper.mock.mockedAzureAdToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PdlClientTest {

    private val externalMockEnvironment = ExternalMockEnvironment()
    private val valkeyMock = externalMockEnvironment.valkeyStore
    private val azureAdClient = mockk<AzureAdClient>()
    private val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        valkeyStore = valkeyMock,
        baseUrl = externalMockEnvironment.environment.pdlUrl,
        clientId = externalMockEnvironment.environment.pdlClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    @BeforeEach
    fun beforeEach() {
        coEvery {
            azureAdClient.getSystemToken(any())
        } returns mockedAzureAdToken
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `hasAdressebeskyttelse returns true when cached value is true`() {
        every { valkeyMock.getObject<PdlHentPerson>(any()) } returns generatePdlHentPerson(
            pdlPersonNavn = null,
            personident = ARBEIDSTAKER_PERSONIDENT,
            adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.STRENGT_FORTROLIG),
            doedsdato = null,
            tilrettelagtKommunikasjon = null,
            sikkerhetstiltak = emptyList(),
        )

        runBlocking {
            assertTrue(
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_ADRESSEBESKYTTET,
                    callId = "callId",
                )!!
            )
        }
        verify(exactly = 1) { valkeyMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 0) { valkeyMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }

    @Test
    fun `hasAdressebeskyttelse returns false when cached value is false`() {
        every { valkeyMock.getObject<PdlHentPerson>(any()) } returns generatePdlHentPerson(
            pdlPersonNavn = null,
            personident = ARBEIDSTAKER_PERSONIDENT,
            adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.UGRADERT),
            doedsdato = null,
            tilrettelagtKommunikasjon = null,
            sikkerhetstiltak = emptyList(),
        )

        runBlocking {
            assertFalse(
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    callId = "callId",
                )!!
            )
        }
        verify(exactly = 1) { valkeyMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 0) { valkeyMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }

    @Test
    fun `hasAdressebeskyttelse returns false and caches value when no cached value and arbeidstaker ikke adressebeskyttet`() {
        every { valkeyMock.getObject<PdlHentPerson>(any()) } returns null
        justRun { valkeyMock.setObject<PdlHentPerson>(any(), any(), any()) }

        runBlocking {
            assertFalse(
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    callId = "callId",
                )!!
            )
        }
        verify(exactly = 1) { valkeyMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 1) { valkeyMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }

    @Test
    fun `hasAdressebeskyttelse returns true and caches value when no cached value and arbeidstaker adressebeskyttet`() {
        every { valkeyMock.getObject<PdlHentPerson>(any()) } returns null
        justRun { valkeyMock.setObject<PdlHentPerson>(any(), any(), any()) }

        runBlocking {
            assertTrue(
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_ADRESSEBESKYTTET,
                    callId = "callId",
                )!!
            )
        }
        verify(exactly = 1) { valkeyMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 1) { valkeyMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }

    @Test
    fun `hasAdressebeskyttelse returns null and doesnt cache when arbeidstaker returns error from pdl`() {
        every { valkeyMock.getObject<PdlHentPerson>(any()) } returns null
        justRun { valkeyMock.setObject<PdlHentPerson>(any(), any(), any()) }

        runBlocking {
            assertNull(
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_PDL_ERROR,
                    callId = "callId",
                )
            )
        }
        verify(exactly = 1) { valkeyMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 0) { valkeyMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }
}
