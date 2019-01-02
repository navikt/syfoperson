package no.nav.syfo.config.caching

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisNode
import org.springframework.data.redis.connection.RedisSentinelConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig

    private val REDIS_SENTINEL_PORT : Int = System.getProperty("redisSentinelPort").toInt()
    private val REDIS_SENTINEL_HOST : String = "rfs-syfoperson"
    private val REDIS_MASTER : String = System.getProperty("redisMaster")
    private val CACHE_NAMES : Set<String> = arrayOf("redisCache").toSet()
    private val ENTRY_TTL : Duration = Duration.ofMillis(60000)

    @Bean
    fun cacheManager(lettuceConnectionFactory: LettuceConnectionFactory) : RedisCacheManager {
        val redisCacheConfig : RedisCacheConfiguration = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(ENTRY_TTL)
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(lettuceConnectionFactory)
                .cacheDefaults(redisCacheConfig)
                .initialCacheNames(CACHE_NAMES)
                .build()
    }

    @Bean
    fun lettuceConnectionFactory() : LettuceConnectionFactory {
        return LettuceConnectionFactory(RedisSentinelConfiguration()
                .master(REDIS_MASTER)
                .sentinel(RedisNode(REDIS_SENTINEL_HOST, REDIS_SENTINEL_PORT)))
    }



