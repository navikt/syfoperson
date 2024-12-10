package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.client.veiledertilgang.Tilgang
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient.Companion.TILGANGSKONTROLL_PERSON_LIST_PATH
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient.Companion.TILGANGSKONTROLL_PERSON_PATH
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PDL_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER

fun MockRequestHandleScope.tilgangskontrollResponse(request: HttpRequestData): HttpResponseData {
    val requestUrl = request.url.encodedPath

    return when {
        requestUrl.endsWith(TILGANGSKONTROLL_PERSON_PATH) -> {
            when (request.headers[NAV_PERSONIDENT_HEADER]) {
                UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS.value -> respond(Tilgang(erGodkjent = false))
                else -> respond(Tilgang(erGodkjent = true))
            }
        }
        requestUrl.endsWith(TILGANGSKONTROLL_PERSON_LIST_PATH) -> {
            respond(
                listOf(
                    ARBEIDSTAKER_PERSONIDENT.value,
                    ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value,
                    ARBEIDSTAKER_ADRESSEBESKYTTET.value,
                    ARBEIDSTAKER_PDL_ERROR.value,
                )
            )
        }
        else -> error("Unhandled path $requestUrl")
    }
}
