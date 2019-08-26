package no.nav.syfo.config.cache

import io.lettuce.core.ClientOptions
import io.lettuce.core.SocketOptions
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachingConfigurerSupport
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import java.time.Duration

@Configuration
class RedisCache : CachingConfigurerSupport() {

    private val REDIS_INSTANCE = "syfoperson-redis"
    private val REDIS_PORT = 6379

    @Bean
    fun redisCacheManager(connectionFactory: RedisConnectionFactory) : CacheManager {
        return RedisCacheManager.builder(connectionFactory).build()
    }

    @Bean
    fun redisConnectionFactory(redisStandaloneConfiguration: RedisStandaloneConfiguration,
                               lettuceClientConfiguration: LettuceClientConfiguration) : RedisConnectionFactory {
        val factory = LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration)
        factory.setShareNativeConnection(true)
        return factory
    }

    @Bean
    fun settingRedisStandaloneConfiguration() : RedisStandaloneConfiguration {
        return RedisStandaloneConfiguration(REDIS_INSTANCE, REDIS_PORT)
    }

    @Bean
    fun poolingClientConfiguration() : LettuceClientConfiguration {
        return LettucePoolingClientConfiguration.builder()
                .poolConfig(getPoolConfig())
                .clientOptions(ClientOptions.builder()
                        .autoReconnect(true)
                        .cancelCommandsOnReconnectFailure(true)
                        .pingBeforeActivateConnection(true)
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .suspendReconnectOnProtocolFailure(false)
                        .socketOptions(SocketOptions.builder().connectTimeout(Duration.ofMillis(500)).build())
                        .build()
                ).build()
    }

    private fun getPoolConfig() : GenericObjectPoolConfig<Any> {
        val genericObjectPoolConfig = GenericObjectPoolConfig<Any>()
        genericObjectPoolConfig.testOnReturn = false
        genericObjectPoolConfig.testOnCreate = false
        genericObjectPoolConfig.testWhileIdle = false
        genericObjectPoolConfig.testOnBorrow = false
        genericObjectPoolConfig.maxTotal = 16
        genericObjectPoolConfig.maxIdle = 8
        genericObjectPoolConfig.minIdle = 8
        genericObjectPoolConfig.timeBetweenEvictionRunsMillis = 10000
        genericObjectPoolConfig.minEvictableIdleTimeMillis = 6000
        return genericObjectPoolConfig
    }

    @Bean
    override fun errorHandler() : CacheErrorHandler {
        return RedisCacheErrorHandler()
    }

}
