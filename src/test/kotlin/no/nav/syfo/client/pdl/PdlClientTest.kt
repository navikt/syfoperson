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
import no.nav.syfo.testhelper.generatePdlHentPerson
import no.nav.syfo.testhelper.startExternalMocks
import no.nav.syfo.testhelper.stopExternalMocks
import org.junit.jupiter.api.*
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

class PdlClientTest {

    private val externalMockEnvironment = ExternalMockEnvironment()
    private val cacheMock = mockk<ValkeyStore>(relaxed = true)
    private val azureAdClient = mockk<AzureAdClient>()
    private val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        valkeyStore = cacheMock,
        baseUrl = externalMockEnvironment.environment.pdlUrl,
        clientId = externalMockEnvironment.environment.pdlClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    @BeforeEach
    fun beforeEach() {
        coEvery {
            azureAdClient.getSystemToken(any())
        } returns AzureAdToken(
            accessToken = "token",
            expires = LocalDateTime.now().plusDays(1)
        )
        externalMockEnvironment.startExternalMocks()
        clearMocks(cacheMock)
    }

    @AfterEach
    fun afterEach() {
        externalMockEnvironment.stopExternalMocks()
    }

    @Test
    fun `hasAdressebeskyttelse returns true when cached value is true`() {
        every { cacheMock.getObject<PdlHentPerson>(any()) } returns generatePdlHentPerson(
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
        verify(exactly = 1) { cacheMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 0) { cacheMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }

    @Test
    fun `hasAdressebeskyttelse returns false when cached value is false`() {
        every { cacheMock.getObject<PdlHentPerson>(any()) } returns generatePdlHentPerson(
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
        verify(exactly = 1) { cacheMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 0) { cacheMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }

    @Test
    fun `hasAdressebeskyttelse returns false and caches value when no cached value and arbeidstaker ikke adressebeskyttet`() {
        every { cacheMock.getObject<PdlHentPerson>(any()) } returns null
        justRun { cacheMock.setObject<PdlHentPerson>(any(), any(), any()) }

        runBlocking {
            assertFalse(
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    callId = "callId",
                )!!
            )
        }
        verify(exactly = 1) { cacheMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 1) { cacheMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }

    @Test
    fun `hasAdressebeskyttelse returns true and caches value when no cached value and arbeidstaker adressebeskyttet`() {
        every { cacheMock.getObject<PdlHentPerson>(any()) } returns null
        justRun { cacheMock.setObject<PdlHentPerson>(any(), any(), any()) }

        runBlocking {
            assertTrue(
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_ADRESSEBESKYTTET,
                    callId = "callId",
                )!!
            )
        }
        verify(exactly = 1) { cacheMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 1) { cacheMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }

    @Test
    fun `hasAdressebeskyttelse returns null and doesnt cache when arbeidstaker returns error from pdl`() {
        every { cacheMock.getObject<PdlHentPerson>(any()) } returns null
        justRun { cacheMock.setObject<PdlHentPerson>(any(), any(), any()) }

        runBlocking {
            assertNull(
                pdlClient.hasAdressebeskyttelse(
                    personIdent = ARBEIDSTAKER_PDL_ERROR,
                    callId = "callId",
                )
            )
        }
        verify(exactly = 1) { cacheMock.getObject<PdlHentPerson>(any()) }
        verify(exactly = 0) { cacheMock.setObject<PdlHentPerson>(any(), any(), any()) }
    }
}
