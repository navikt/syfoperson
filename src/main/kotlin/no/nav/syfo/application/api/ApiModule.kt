package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.authentication.*
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.application.metric.api.registerMetricApi
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.krr.KRRClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.client.wellknown.WellKnown
import no.nav.syfo.person.api.registrerPersonApi
import no.nav.syfo.person.skjermingskode.SkjermingskodeService
import redis.clients.jedis.*

fun Application.apiModule(
    applicationState: ApplicationState,
    environment: Environment,
    wellKnownInternalAzureAD: WellKnown,
) {
    installMetrics()
    installCallId()
    installContentNegotiation()
    installJwtAuthentication(
        jwtIssuerList = listOf(
            JwtIssuer(
                acceptedAudienceList = listOf(environment.azureAppClientId),
                jwtIssuerType = JwtIssuerType.INTERNAL_AZUREAD,
                wellKnown = wellKnownInternalAzureAD,
            ),
        ),
    )
    installStatusPages()
    val redisStore = RedisStore(
        jedisPool = JedisPool(
            JedisPoolConfig(),
            environment.redisHost,
            environment.redisPort,
            Protocol.DEFAULT_TIMEOUT,
            environment.redisSecret
        )
    )

    val azureAdClient = AzureAdClient(
        azureAppClientId = environment.azureAppClientId,
        azureAppClientSecret = environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = environment.azureOpenidConfigTokenEndpoint,
        redisStore = redisStore,
    )

    val krrClient = KRRClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.krrUrl,
        clientId = environment.krrClientId,
    )
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.pdlUrl,
        clientId = environment.pdlClientId,
    )
    val skjermedePersonerPipClient = SkjermedePersonerPipClient(
        azureAdClient = azureAdClient,
        redisStore = redisStore,
        baseUrl = environment.skjermedePersonerPipUrl,
        clientId = environment.skjermedePersonerPipClientId,
    )
    val skjermingskodeService = SkjermingskodeService(
        skjermedePersonerPipClient = skjermedePersonerPipClient,
    )
    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.syfotilgangskontrollUrl,
        clientId = environment.syfotilgangskontrollClientId,
    )

    routing {
        registerPodApi(
            applicationState = applicationState,
        )
        registerMetricApi()
        authenticate(JwtIssuerType.INTERNAL_AZUREAD.name) {
            registrerPersonApi(
                krrClient = krrClient,
                pdlClient = pdlClient,
                skjermingskodeService = skjermingskodeService,
                skjermedePersonerPipClient = skjermedePersonerPipClient,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            )
        }
    }
}
