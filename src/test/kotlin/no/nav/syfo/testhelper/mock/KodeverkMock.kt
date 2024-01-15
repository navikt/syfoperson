package no.nav.syfo.testhelper.mock

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.kodeverk.*
import no.nav.syfo.client.kodeverk.KodeverkClient.Companion.KODEVERK_POSTNUMMER_BETYDNINGER_PATH
import no.nav.syfo.testhelper.getRandomPort
import no.nav.syfo.util.NAV_CALL_ID_HEADER

class KodeverkMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val name = "kodeverk"

    private val kodeverkResponse = KodeverkBetydninger(
        betydninger = mapOf(
            "1001" to listOf(
                Betydning(
                    gyldigFra = "1900-01-01",
                    gyldigTil = "9999-12-31",
                    beskrivelser = mapOf(
                        "nb" to Beskrivelse(
                            term = "OSLO",
                            tekst = "OSLO",
                        ),
                    ),
                ),
            ),
        ),
    )

    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            get(KODEVERK_POSTNUMMER_BETYDNINGER_PATH) {
                when {
                    call.request.headers[NAV_CALL_ID_HEADER] == "500" -> {
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                    else -> {
                        call.respond(kodeverkResponse)
                    }
                }
            }
        }
    }
}
