package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.client.kodeverk.Beskrivelse
import no.nav.syfo.client.kodeverk.Betydning
import no.nav.syfo.client.kodeverk.KodeverkBetydninger
import no.nav.syfo.util.NAV_CALL_ID_HEADER

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

fun MockRequestHandleScope.kodeverkMockResponse(request: HttpRequestData): HttpResponseData {
    return when (request.headers[NAV_CALL_ID_HEADER]) {
        "500" -> respondError(HttpStatusCode.InternalServerError)
        else -> respond(kodeverkResponse)
    }
}
