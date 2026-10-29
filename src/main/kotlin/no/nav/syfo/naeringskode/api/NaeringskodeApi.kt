package no.nav.syfo.naeringskode.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.client.ssb.SsbException
import no.nav.syfo.naeringskode.NaeringskodeService
import no.nav.syfo.naeringskode.NaeringskodeStandard
import org.slf4j.LoggerFactory

const val apiNaeringskodeBasePath = "/syfoperson/api/v2/naeringskode"

private val codePattern = Regex("[A-Z]|[0-9]{2}(?:\\.[0-9]{1,3})?")
private val log = LoggerFactory.getLogger("no.nav.syfo.naeringskode.api")

fun Route.registrerNaeringskodeApi(service: NaeringskodeService) {
    get("$apiNaeringskodeBasePath/{kode}") {
        val code = call.parameters["kode"].orEmpty()
        val standard = NaeringskodeStandard.entries.find {
            it.year.toString() == call.request.queryParameters["standard"]
        }
        if (!codePattern.matches(code) || standard == null) {
            call.respond(HttpStatusCode.BadRequest, "Valid kode and standard=2007 or standard=2025 are required")
            return@get
        }

        try {
            val hierarki = service.getHierarki(code, standard)
            if (hierarki == null) {
                call.respond(HttpStatusCode.NotFound, "Unknown naeringskode for selected standard")
            } else {
                call.respond(hierarki)
            }
        } catch (e: SsbException) {
            log.error("Failed to fetch naeringskode hierarchy", e)
            call.respond(HttpStatusCode.BadGateway, "Failed to fetch naeringskoder from SSB")
        }
    }
}
