package no.nav.syfo.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TimestampService {
    @Cacheable("tsCache")
    fun getTimestamp(): LocalDateTime {
        var localDateTime : LocalDateTime = LocalDateTime.now()
        return localDateTime;
    }
}