package no.nav.syfo.application

import no.nav.syfo.application.cache.RedisConfig
import java.net.URI

data class Environment(
    val azureAppClientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val azureAppClientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val azureAppWellKnownUrl: String = getEnvVar("AZURE_APP_WELL_KNOWN_URL"),
    val azureOpenidConfigTokenEndpoint: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),

    val krrClientId: String = getEnvVar("KRR_CLIENT_ID"),
    val krrUrl: String = getEnvVar("KRR_URL"),

    val pdlClientId: String = getEnvVar("PDL_CLIENT_ID"),
    val pdlUrl: String = getEnvVar("PDL_URL"),

    val skjermedePersonerPipClientId: String = getEnvVar("SKJERMEDEPERSONERPIP_CLIENT_ID"),
    val skjermedePersonerPipUrl: String = getEnvVar("SKJERMEDEPERSONERPIP_URL"),

    val istilgangskontrollClientId: String = getEnvVar("ISTILGANGSKONTROLL_CLIENT_ID"),
    val istilgangskontrollUrl: String = getEnvVar("ISTILGANGSKONTROLL_URL"),

    val kodeverkClientId: String = getEnvVar("KODEVERK_CLIENT_ID"),
    val kodeverkUrl: String = getEnvVar("KODEVERK_URL"),
    val redisConfig: RedisConfig = RedisConfig(
        redisUri = URI(getEnvVar("REDIS_URI_CACHE")),
        redisDB = 23, // se https://github.com/navikt/istilgangskontroll/blob/master/README.md
        redisUsername = getEnvVar("REDIS_USERNAME_CACHE"),
        redisPassword = getEnvVar("REDIS_PASSWORD_CACHE"),
    ),
)

fun getEnvVar(
    varName: String,
    defaultValue: String? = null,
) = System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
