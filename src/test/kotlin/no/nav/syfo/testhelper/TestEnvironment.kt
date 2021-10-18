package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import java.net.ServerSocket

fun testEnvironment(
    azureOpenIdTokenEndpoint: String = "azureTokenEndpoint",
    isproxyUrl: String = "isproxy",
    pdlUrl: String = "pdl",
    skjermedePersonerPipUrl: String = "skjermedepersonerpip",
    syfotilgangskontrollUrl: String = "tilgangskontroll",
) = Environment(
    azureAppClientId = "syfoperson-client-id",
    azureAppClientSecret = "syfoperson-secret",
    azureAppWellKnownUrl = "wellknown",
    azureOpenidConfigTokenEndpoint = azureOpenIdTokenEndpoint,
    isproxyClientId = "dev-fss.teamsykefravr.isproxy",
    isproxyUrl = isproxyUrl,
    pdlClientId = "dev-fss.pdl.pdl-api",
    pdlUrl = pdlUrl,
    skjermedePersonerPipClientId = "dev-gcp.nom.skjermede-personer-pip",
    skjermedePersonerPipUrl = skjermedePersonerPipUrl,
    syfotilgangskontrollClientId = "dev-fss.teamsykefravr.syfo-tilgangskontroll",
    syfotilgangskontrollUrl = syfotilgangskontrollUrl,
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
