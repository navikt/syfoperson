package no.nav.syfo.config.cache

import no.nav.syfo.service.PersonService
import org.slf4j.LoggerFactory
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.lang.NonNull
import org.springframework.lang.Nullable
import java.lang.RuntimeException

class RedisCacheErrorHandler : CacheErrorHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(RedisCacheErrorHandler::class.java)
    }

    override fun handleCacheGetError(@NonNull exception: RuntimeException, @NonNull cache: Cache, @NonNull o: Any) {
        LOG.warn("Feil ved Cache Get operasjon. CacheNavn=${cache.name}, feilklasse=${exception.javaClass.simpleName}, feilmelding=${exception.message}")
    }

    override fun handleCachePutError(@NonNull exception: RuntimeException, @NonNull cache: Cache,
                                     @NonNull key: Any, @Nullable value: Any?) {
        LOG.warn("Feil ved Cache Put operasjon. CacheNavn=${cache.name}, feilklasse=${exception.javaClass.simpleName}, feilmelding=${exception.message}")
    }

    override fun handleCacheEvictError(@NonNull exception: RuntimeException, @NonNull cache: Cache,
                                       @NonNull key: Any) {
        LOG.warn("Feil ved Cache Evict operasjon. CacheNavn=${cache.name}, feilklasse=${exception.javaClass.simpleName}, feilmelding=${exception.message}")
    }

    override fun handleCacheClearError(@NonNull exception: RuntimeException, @NonNull cache: Cache) {
        LOG.warn("Feil ved Cache Clear operasjon. CacheNavn=${cache.name}, feilklasse=${exception.javaClass.simpleName}, feilmelding=${exception.message}")
    }

}
