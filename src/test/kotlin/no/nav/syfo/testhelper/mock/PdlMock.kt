package no.nav.syfo.testhelper.mock

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.pdl.*
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_DOD
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PDL_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_SIKKERHETSTILTAK
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON
import java.time.LocalDate

class PdlMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val name = "pdl"
    val personResponseDefault = generatePdlPersonResponse()

    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            post {
                val pdlRequest = call.receive<PdlRequest>()
                call.respond(
                    if (ARBEIDSTAKER_ADRESSEBESKYTTET.value == pdlRequest.variables.ident) {
                        generatePdlPersonResponse(Gradering.STRENGT_FORTROLIG)
                    } else if (ARBEIDSTAKER_DOD.value == pdlRequest.variables.ident) {
                        generatePdlPersonResponse(doedsdato = LocalDate.now())
                    } else if (ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON.value == pdlRequest.variables.ident) {
                        generatePdlPersonResponse(tilrettelagtKommunikasjon = generatePdlTilrettelagtKommunikasjon())
                    } else if (ARBEIDSTAKER_SIKKERHETSTILTAK.value == pdlRequest.variables.ident) {
                        generatePdlPersonResponse(sikkerhetstiltak = generatePdlSikkerhetsiltak())
                    } else if (ARBEIDSTAKER_PDL_ERROR.value == pdlRequest.variables.ident) {
                        generatePdlPersonResponseError()
                    } else {
                        personResponseDefault
                    }
                )
            }
        }
    }
}
