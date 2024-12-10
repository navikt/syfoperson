package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.cache.RedisConfig
import java.net.URI

fun testEnvironment() = Environment(
    azureAppClientId = "syfoperson-client-id",
    azureAppClientSecret = "syfoperson-secret",
    azureAppWellKnownUrl = "wellknown",
    azureOpenidConfigTokenEndpoint = "azureTokenEndpoint",
    krrClientId = "dev-gcp.team-rocket.digdir-krr-proxy",
    krrUrl = "krrUrl",
    pdlClientId = "dev-fss.pdl.pdl-api",
    pdlUrl = "pdlUrl",
    skjermedePersonerPipClientId = "dev-gcp.nom.skjermede-personer-pip",
    skjermedePersonerPipUrl = "skjermedePersonerPipUrl",
    istilgangskontrollClientId = "dev-gcp.teamsykefravr.istilgangskontroll",
    istilgangskontrollUrl = "istilgangskontrollUrl",
    kodeverkClientId = "dev-gcp.team-rocket.kodeverk-api",
    kodeverkUrl = "kodeverkUrl",
    redisConfig = RedisConfig(
        redisUri = URI("http://localhost:6379"),
        redisDB = 0,
        redisUsername = "redisUser",
        redisPassword = "redisPassword",
        ssl = false,
    ),
)

fun testAppState() = ApplicationState(
    alive = true,
    ready = true,
)
