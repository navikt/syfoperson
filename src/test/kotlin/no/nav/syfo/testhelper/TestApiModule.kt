package no.nav.syfo.testhelper

import io.ktor.server.application.*
import no.nav.syfo.application.api.apiModule

fun Application.testApiModule(
    externalMockEnvironment: ExternalMockEnvironment,
) = this.apiModule(
    applicationState = externalMockEnvironment.applicationState,
    environment = externalMockEnvironment.environment,
    wellKnownInternalAzureAD = externalMockEnvironment.wellKnownInternalAzureAD,
    krrClient = externalMockEnvironment.krrClient,
    pdlClient = externalMockEnvironment.pdlClient,
    skjermedePersonerPipClient = externalMockEnvironment.skjermedePersonerPipClient,
    kodeverkClient = externalMockEnvironment.kodeverkClient,
    veilederTilgangskontrollClient = externalMockEnvironment.veilederTilgangskontrollClient,
)
