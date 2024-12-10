package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.testhelper.mock.*
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class ExternalMockEnvironment {
    val applicationState: ApplicationState = testAppState()

    val environment = testEnvironment()
    val mockHttpClient = getMockHttpClient(env = environment)

    val redisConfig = environment.redisConfig
    val redisCache = RedisStore(
        JedisPool(
            JedisPoolConfig(),
            HostAndPort(redisConfig.host, redisConfig.port),
            DefaultJedisClientConfig.builder()
                .ssl(redisConfig.ssl)
                .password(redisConfig.redisPassword)
                .database(redisConfig.redisDB)
                .build()
        )
    )
    val redisServer = testRedis(
        port = environment.redisConfig.redisUri.port,
        secret = environment.redisConfig.redisPassword,
    )

    val wellKnownInternalAzureAD = wellKnownInternalAzureAD()
}

fun ExternalMockEnvironment.startExternalMocks() {
    this.redisServer.start()
}

fun ExternalMockEnvironment.stopExternalMocks() {
    this.redisServer.stop()
}
