package no.nav.syfo.application.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.authentication.*
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.application.metric.api.registerMetricApi
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.kodeverk.KodeverkClient
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
        redisStore = redisStore,
        baseUrl = environment.krrUrl,
        clientId = environment.krrClientId,
    )
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        redisStore = redisStore,
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
        pdlClient = pdlClient,
    )
    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.istilgangskontrollUrl,
        clientId = environment.istilgangskontrollClientId,
    )

    val kodeverkClient = KodeverkClient(
        redisStore = redisStore,
        baseUrl = environment.kodeverkUrl,
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
                kodeverkClient = kodeverkClient,
            )
        }
    }
}
