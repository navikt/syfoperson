package no.nav.syfo

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.kodeverk.KodeverkClient
import no.nav.syfo.client.krr.KRRClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.client.wellknown.getWellKnown
import org.slf4j.LoggerFactory
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.util.concurrent.TimeUnit

const val applicationPort = 8080

fun main() {
    val applicationState = ApplicationState()
    val environment = Environment()
    val valkeyConfig = environment.valkeyConfig
    val cache = ValkeyStore(
        JedisPool(
            JedisPoolConfig(),
            HostAndPort(valkeyConfig.host, valkeyConfig.port),
            DefaultJedisClientConfig.builder()
                .ssl(valkeyConfig.ssl)
                .user(valkeyConfig.valkeyUsername)
                .password(valkeyConfig.valkeyPassword)
                .database(valkeyConfig.valkeyDB)
                .build()
        )
    )
    val azureAdClient = AzureAdClient(
        azureAppClientId = environment.azureAppClientId,
        azureAppClientSecret = environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = environment.azureOpenidConfigTokenEndpoint,
        valkeyStore = cache,
    )
    val krrClient = KRRClient(
        azureAdClient = azureAdClient,
        valkeyStore = cache,
        baseUrl = environment.krrUrl,
        clientId = environment.krrClientId,
    )
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        valkeyStore = cache,
        baseUrl = environment.pdlUrl,
        clientId = environment.pdlClientId,
    )
    val skjermedePersonerPipClient = SkjermedePersonerPipClient(
        azureAdClient = azureAdClient,
        valkeyStore = cache,
        baseUrl = environment.skjermedePersonerPipUrl,
        clientId = environment.skjermedePersonerPipClientId,
    )
    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.istilgangskontrollUrl,
        clientId = environment.istilgangskontrollClientId,
    )
    val kodeverkClient = KodeverkClient(
        azureAdClient = azureAdClient,
        valkeyStore = cache,
        baseUrl = environment.kodeverkUrl,
        clientId = environment.kodeverkClientId,
    )

    val applicationEngineEnvironment = applicationEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        config = HoconApplicationConfig(ConfigFactory.load())
    }
    val server = embeddedServer(
        Netty,
        environment = applicationEngineEnvironment,
        configure = {
            connector {
                port = applicationPort
            }
            connectionGroupSize = 8
            workerGroupSize = 8
            callGroupSize = 16
        },
        module = {
            val wellKnownInternalAzureAD = getWellKnown(
                wellKnownUrl = environment.azureAppWellKnownUrl
            )
            apiModule(
                applicationState = applicationState,
                environment = environment,
                wellKnownInternalAzureAD = wellKnownInternalAzureAD,
                krrClient = krrClient,
                pdlClient = pdlClient,
                skjermedePersonerPipClient = skjermedePersonerPipClient,
                kodeverkClient = kodeverkClient,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            )
            monitor.subscribe(ApplicationStarted) { application ->
                applicationState.ready = true
                application.environment.log.info("Application is ready, running Java VM ${Runtime.version()}")
            }
        }
    )

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
        }
    )

    server.start(wait = true)
}
