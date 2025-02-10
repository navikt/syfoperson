package no.nav.syfo.testhelper

import io.ktor.server.application.*
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.kodeverk.KodeverkClient
import no.nav.syfo.client.krr.KRRClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient

fun Application.testApiModule(
    externalMockEnvironment: ExternalMockEnvironment,
) {
    val azureAdClient = AzureAdClient(
        azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
        azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
        valkeyStore = externalMockEnvironment.redisCache,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val krrClient = KRRClient(
        azureAdClient = azureAdClient,
        valkeyStore = externalMockEnvironment.redisCache,
        baseUrl = externalMockEnvironment.environment.krrUrl,
        clientId = externalMockEnvironment.environment.krrClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        valkeyStore = externalMockEnvironment.redisCache,
        baseUrl = externalMockEnvironment.environment.pdlUrl,
        clientId = externalMockEnvironment.environment.pdlClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val skjermedePersonerPipClient = SkjermedePersonerPipClient(
        azureAdClient = azureAdClient,
        valkeyStore = externalMockEnvironment.redisCache,
        baseUrl = externalMockEnvironment.environment.skjermedePersonerPipUrl,
        clientId = externalMockEnvironment.environment.skjermedePersonerPipClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.istilgangskontrollUrl,
        clientId = externalMockEnvironment.environment.istilgangskontrollClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val kodeverkClient = KodeverkClient(
        azureAdClient = azureAdClient,
        valkeyStore = externalMockEnvironment.redisCache,
        baseUrl = externalMockEnvironment.environment.kodeverkUrl,
        clientId = externalMockEnvironment.environment.kodeverkClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    this.apiModule(
        applicationState = externalMockEnvironment.applicationState,
        environment = externalMockEnvironment.environment,
        wellKnownInternalAzureAD = externalMockEnvironment.wellKnownInternalAzureAD,
        krrClient = krrClient,
        pdlClient = pdlClient,
        skjermedePersonerPipClient = skjermedePersonerPipClient,
        kodeverkClient = kodeverkClient,
        veilederTilgangskontrollClient = veilederTilgangskontrollClient,
    )
}
