package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.testhelper.mock.*
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class ExternalMockEnvironment {
    val applicationState: ApplicationState = testAppState()

    val environment = testEnvironment()
    val mockHttpClient = getMockHttpClient(env = environment)

    val redisConfig = environment.valkeyConfig
    val redisCache = ValkeyStore(
        JedisPool(
            JedisPoolConfig(),
            HostAndPort(redisConfig.host, redisConfig.port),
            DefaultJedisClientConfig.builder()
                .ssl(redisConfig.ssl)
                .password(redisConfig.valkeyPassword)
                .database(redisConfig.valkeyDB)
                .build()
        )
    )
    val redisServer = testRedis(
        port = environment.valkeyConfig.valkeyUri.port,
        secret = environment.valkeyConfig.valkeyPassword,
    )

    val wellKnownInternalAzureAD = wellKnownInternalAzureAD()
}

fun ExternalMockEnvironment.startExternalMocks() {
    this.redisServer.start()
}

fun ExternalMockEnvironment.stopExternalMocks() {
    this.redisServer.stop()
}
