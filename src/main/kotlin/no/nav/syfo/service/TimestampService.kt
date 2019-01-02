package no.nav.syfo.service

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TimestampService {
    fun getTimestamp(): LocalDateTime {
        var localDateTime : LocalDateTime = LocalDateTime.now()
        return localDateTime;
    }
}