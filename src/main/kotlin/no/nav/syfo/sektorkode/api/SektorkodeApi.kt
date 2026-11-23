package no.nav.syfo.sektorkode.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.client.ssb.SsbException
import no.nav.syfo.sektorkode.SektorkodeService
import no.nav.syfo.sektorkode.SektorkodeStandard
import org.slf4j.LoggerFactory

const val apiSektorkodeBasePath = "/syfoperson/api/v2/sektorkode"

private val codePattern = Regex("[0-9]{3,4}|[A-Z][A-Z_-]{0,19}")
private val log = LoggerFactory.getLogger("no.nav.syfo.sektorkode.api")

fun Route.registrerSektorkodeApi(service: SektorkodeService) {
    get("$apiSektorkodeBasePath/{kode}") {
        val code = call.parameters["kode"].orEmpty()
        val standardParameter = call.request.queryParameters.getAll("standard")?.singleOrNull()
        val standard = SektorkodeStandard.entries.find { it.year.toString() == standardParameter }
        if (!codePattern.matches(code) || standard == null) {
            val supportedStandards = SektorkodeStandard.entries.joinToString(", ") { it.year.toString() }
            call.respondProblem(
                HttpStatusCode.BadRequest,
                "Valid kode and exactly one standard ($supportedStandards) are required",
            )
            return@get
        }

        try {
            val hierarki = service.getHierarki(code, standard)
            if (hierarki == null) {
                call.respondProblem(HttpStatusCode.NotFound, "Unknown sektorkode for selected standard")
            } else {
                call.respond(hierarki)
            }
        } catch (e: SsbException) {
            log.error("Failed to fetch sektorkode hierarchy", e)
            call.respondProblem(HttpStatusCode.BadGateway, "Failed to fetch sektorkoder from SSB")
        }
    }
}

private suspend fun ApplicationCall.respondProblem(status: HttpStatusCode, detail: String) {
    respond(
        status,
        SektorkodeProblem(title = status.description, status = status.value, detail = detail),
    )
}

private data class SektorkodeProblem(
    val title: String,
    val status: Int,
    val detail: String,
    val type: String = "about:blank",
)
