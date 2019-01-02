package no.nav.syfo.controller

import no.nav.syfo.service.TimestampService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import javax.inject.Inject
import javax.ws.rs.core.MediaType.APPLICATION_JSON

@RestController
@RequestMapping(value = ["/api"])
class TimestampController @Inject
constructor(val timestampService: TimestampService) {

    @ResponseBody
    @GetMapping(value = ["/timestamp"], produces = [APPLICATION_JSON])
    fun getTimestamp(): LocalDateTime {
        var localDateTime: LocalDateTime = timestampService.getTimestamp()
        return localDateTime
    }
}
