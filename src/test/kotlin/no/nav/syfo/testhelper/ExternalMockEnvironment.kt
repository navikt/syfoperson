package no.nav.syfo.testhelper

import io.mockk.mockk
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.kodeverk.KodeverkClient
import no.nav.syfo.client.krr.KRRClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.testhelper.mock.*

class ExternalMockEnvironment {
    val applicationState: ApplicationState = testAppState()

    val environment = testEnvironment()
    val mockHttpClient = getMockHttpClient(env = environment)

    val valkeyStore = mockk<ValkeyStore>(relaxed = true)

    val azureAdClient = AzureAdClient(
        azureAppClientId = environment.azureAppClientId,
        azureAppClientSecret = environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = environment.azureOpenidConfigTokenEndpoint,
        valkeyStore = valkeyStore,
        httpClient = mockHttpClient,
    )

    val krrClient = KRRClient(
        azureAdClient = azureAdClient,
        valkeyStore = valkeyStore,
        baseUrl = environment.krrUrl,
        clientId = environment.krrClientId,
        httpClient = mockHttpClient,
    )
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        valkeyStore = valkeyStore,
        baseUrl = environment.pdlUrl,
        clientId = environment.pdlClientId,
        httpClient = mockHttpClient,
    )
    val skjermedePersonerPipClient = SkjermedePersonerPipClient(
        azureAdClient = azureAdClient,
        valkeyStore = valkeyStore,
        baseUrl = environment.skjermedePersonerPipUrl,
        clientId = environment.skjermedePersonerPipClientId,
        httpClient = mockHttpClient,
    )
    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.istilgangskontrollUrl,
        clientId = environment.istilgangskontrollClientId,
        httpClient = mockHttpClient,
    )
    val kodeverkClient = KodeverkClient(
        azureAdClient = azureAdClient,
        valkeyStore = valkeyStore,
        baseUrl = environment.kodeverkUrl,
        clientId = environment.kodeverkClientId,
        httpClient = mockHttpClient,
    )

    val wellKnownInternalAzureAD = wellKnownInternalAzureAD()
}
