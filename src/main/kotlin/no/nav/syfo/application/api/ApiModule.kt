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
import no.nav.syfo.client.dkif.DkifClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.syketilfelle.SyketilfelleClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.client.wellknown.WellKnown
import no.nav.syfo.person.api.registrerPersonApi
import no.nav.syfo.person.oppfolgingstilfelle.OppfolgingstilfelleService
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

    val dkifClient = DkifClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.isproxyUrl,
        clientId = environment.isproxyClientId,
    )
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.pdlUrl,
        clientId = environment.pdlClientId,
    )
    val syketilfelleClient = SyketilfelleClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.isproxyUrl,
        clientId = environment.isproxyClientId
    )
    val oppfolgingstilfelleService = OppfolgingstilfelleService(
        pdlClient = pdlClient,
        syketilfelleClient = syketilfelleClient,
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
                dkifClient = dkifClient,
                pdlClient = pdlClient,
                oppfolgingstilfelleService = oppfolgingstilfelleService,
                skjermingskodeService = skjermingskodeService,
                skjermedePersonerPipClient = skjermedePersonerPipClient,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            )
        }
    }
}
