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
class CacheConfig {

    private val redisSentinelPort: Int = System.getProperty("redisSentinelPort").toInt()
    private val redisSentinelHost: String = System.getProperty("redisSentinelHost")
    private val redisMaster: String = System.getProperty("redisMaster")
    private val cacheNames: Set<String> = arrayOf("redisCache").toSet()
    private val entryTTL: Duration = Duration.ofMillis(60000)

    @Bean
    fun cacheManager(lettuceConnectionFactory: LettuceConnectionFactory): RedisCacheManager {
        val redisCacheConfig: RedisCacheConfiguration = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(entryTTL)
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(lettuceConnectionFactory)
                .cacheDefaults(redisCacheConfig)
                .initialCacheNames(cacheNames)
                .build()
    }

    @Bean
    fun lettuceConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory(RedisSentinelConfiguration()
                .master(redisMaster)
                .sentinel(RedisNode(redisSentinelHost, redisSentinelPort)))
    }

}

