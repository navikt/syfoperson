package no.nav.syfo.testhelper.mock

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.veiledertilgang.Tilgang
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient.Companion.TILGANGSKONTROLL_PERSON_LIST_PATH
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient.Companion.TILGANGSKONTROLL_PERSON_PATH
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PDL_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.getRandomPort
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER

class VeilederTilgangskontrollMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val tilgangFalse = Tilgang(
        erGodkjent = false,
    )
    val tilgangTrue = Tilgang(
        erGodkjent = true,
    )

    val name = "veiledertilgangskontroll"
    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            get(TILGANGSKONTROLL_PERSON_PATH) {
                when {
                    call.request.headers[NAV_PERSONIDENT_HEADER] == UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS.value -> {
                        call.respond(HttpStatusCode.Forbidden, tilgangFalse)
                    }
                    call.request.headers[NAV_PERSONIDENT_HEADER] != null -> {
                        call.respond(tilgangTrue)
                    }
                    else -> {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }
            }
            post(TILGANGSKONTROLL_PERSON_LIST_PATH) {
                call.respond(
                    listOf(
                        ARBEIDSTAKER_PERSONIDENT.value,
                        ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value,
                        ARBEIDSTAKER_ADRESSEBESKYTTET.value,
                        ARBEIDSTAKER_PDL_ERROR.value,
                    )
                )
            }
        }
    }
}
