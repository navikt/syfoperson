package no.nav.syfo.testhelper.mock

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.veiledertilgang.Tilgang
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient.Companion.TILGANGSKONTROLL_PERSON_LIST_PATH
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient.Companion.TILGANGSKONTROLL_PERSON_PATH
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.getRandomPort
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER

class VeilederTilgangskontrollMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val tilgangFalse = Tilgang(
        false,
        ""
    )
    val tilgangTrue = Tilgang(
        true,
        ""
    )

    val name = "veiledertilgangskontroll"
    val server = mockTilgangServer(
        port,
        tilgangFalse,
        tilgangTrue
    )

    private fun mockTilgangServer(
        port: Int,
        tilgangFalse: Tilgang,
        tilgangTrue: Tilgang,
    ): NettyApplicationEngine {
        return embeddedServer(
            factory = Netty,
            port = port
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
                        )
                    )
                }
            }
        }
    }
}