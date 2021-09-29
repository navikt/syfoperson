package no.nav.syfo

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import no.nav.syfo.cache.CacheConfig
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.*

@Configuration
@Import(TokenGeneratorConfiguration::class)
class LocalApplicationConfig {
    @Bean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager(CacheConfig.TOKENS)
    }
}
