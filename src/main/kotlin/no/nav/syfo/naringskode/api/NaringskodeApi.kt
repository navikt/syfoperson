package no.nav.syfo.naringskode.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.client.ssb.SsbException
import no.nav.syfo.naringskode.NaringskodeService
import no.nav.syfo.naringskode.NaringskodeStandard
import org.slf4j.LoggerFactory

const val apiNaringskodeBasePath = "/syfoperson/api/v2/naringskode"

private val codePattern = Regex("[A-Z][A-Z_]?|[0-9]{2}(?:\\.[0-9]{1,3})?")
private val log = LoggerFactory.getLogger("no.nav.syfo.naringskode.api")

fun Route.registrerNaringskodeApi(service: NaringskodeService) {
    get("$apiNaringskodeBasePath/{kode}") {
        val code = call.parameters["kode"].orEmpty()
        val standard = NaringskodeStandard.entries.find {
            it.year.toString() == call.request.queryParameters["standard"]
        }
        if (!codePattern.matches(code) || standard == null) {
            val supportedStandards = NaringskodeStandard.entries.joinToString(", ") { it.year.toString() }
            call.respond(HttpStatusCode.BadRequest, "Valid kode and standard ($supportedStandards) are required")
            return@get
        }

        try {
            val hierarki = service.getHierarki(code, standard)
            if (hierarki == null) {
                call.respond(HttpStatusCode.NotFound, "Unknown naringskode for selected standard")
            } else {
                call.respond(hierarki)
            }
        } catch (e: SsbException) {
            log.error("Failed to fetch naringskode hierarchy", e)
            call.respond(HttpStatusCode.BadGateway, "Failed to fetch naringskoder from SSB")
        }
    }
}
