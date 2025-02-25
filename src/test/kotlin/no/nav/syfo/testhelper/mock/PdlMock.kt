package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.client.pdl.*
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_DOD
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PDL_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_SIKKERHETSTILTAK
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON
import java.time.LocalDate

suspend fun MockRequestHandleScope.pdlMockResponse(request: HttpRequestData): HttpResponseData {
    val pdlRequest = request.receiveBody<PdlRequest>()
    return when (pdlRequest.variables.ident) {
        ARBEIDSTAKER_ADRESSEBESKYTTET.value -> respond(generatePdlPersonResponse(ARBEIDSTAKER_ADRESSEBESKYTTET, Gradering.STRENGT_FORTROLIG))
        ARBEIDSTAKER_DOD.value -> respond(generatePdlPersonResponse(ARBEIDSTAKER_DOD, doedsdato = LocalDate.now()))
        ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON.value -> respond(
            generatePdlPersonResponse(ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON, tilrettelagtKommunikasjon = generatePdlTilrettelagtKommunikasjon())
        )

        ARBEIDSTAKER_SIKKERHETSTILTAK.value -> respond(generatePdlPersonResponse(ARBEIDSTAKER_SIKKERHETSTILTAK, sikkerhetstiltak = generatePdlSikkerhetsiltak()))
        ARBEIDSTAKER_PDL_ERROR.value -> respond(generatePdlPersonResponseError())
        else -> respond(generatePdlPersonResponse(ARBEIDSTAKER_PERSONIDENT))
    }
}
