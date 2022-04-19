package no.nav.syfo.testhelper.mock

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.getRandomPort

class SkjermedePersonerPipMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val name = "skjermedpersonerpip"
    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            get("/skjermet") {
                if (call.request.queryParameters["personident"] == ARBEIDSTAKER_PERSONIDENT.value) {
                    call.respond(true)
                }
                if (call.request.queryParameters["personident"] == ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value) {
                    call.respond(false)
                }
                if (call.request.queryParameters["personident"] == ARBEIDSTAKER_ADRESSEBESKYTTET.value) {
                    call.respond(false)
                }
            }
        }
    }
}
