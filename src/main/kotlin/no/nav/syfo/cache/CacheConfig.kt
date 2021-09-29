package no.nav.syfo.cache

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.*
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration
import java.util.*

@Configuration
@EnableCaching
@Profile("remote")
class CacheConfig {

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val cacheConfigurations: MutableMap<String, RedisCacheConfiguration> = HashMap()
        val defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofHours(12L))

        cacheConfigurations[EGNEANSATTBYFNR] = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofHours(1L))

        cacheConfigurations[TOKENS] = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofHours(1L))

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

    companion object {
        const val EGNEANSATTBYFNR = "egenAnsattByFnr"
        const val TOKENS = "tokens"
    }
}
