package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import java.net.ServerSocket

fun testEnvironment(
    azureOpenIdTokenEndpoint: String = "azureTokenEndpoint",
    krrUrl: String = "krr",
    pdlUrl: String = "pdl",
    skjermedePersonerPipUrl: String = "skjermedepersonerpip",
    istilgangskontrollUrl: String = "tilgangskontroll",
    kodeverkUrl: String = "kodeverk",
) = Environment(
    azureAppClientId = "syfoperson-client-id",
    azureAppClientSecret = "syfoperson-secret",
    azureAppWellKnownUrl = "wellknown",
    azureOpenidConfigTokenEndpoint = azureOpenIdTokenEndpoint,
    krrClientId = "dev-gcp.team-rocket.digdir-krr-proxy",
    krrUrl = krrUrl,
    pdlClientId = "dev-fss.pdl.pdl-api",
    pdlUrl = pdlUrl,
    skjermedePersonerPipClientId = "dev-gcp.nom.skjermede-personer-pip",
    skjermedePersonerPipUrl = skjermedePersonerPipUrl,
    istilgangskontrollClientId = "dev-gcp.teamsykefravr.istilgangskontroll",
    istilgangskontrollUrl = istilgangskontrollUrl,
    kodeverkClientId = "dev-gcp.team-rocket.kodeverk-api",
    kodeverkUrl = kodeverkUrl,
    redisHost = "localhost",
    redisSecret = "password",
)

fun testAppState() = ApplicationState(
    alive = true,
    ready = true,
)

fun getRandomPort() = ServerSocket(0).use {
    it.localPort
}
