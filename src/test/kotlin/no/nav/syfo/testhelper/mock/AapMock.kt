package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import java.time.LocalDate
import no.nav.syfo.client.aap.AapPeriode
import no.nav.syfo.client.aap.AapSak
import no.nav.syfo.client.aap.AapSakerRequest
import no.nav.syfo.client.aap.AapSakerResponse
import no.nav.syfo.client.aap.AapVedtak

suspend fun MockRequestHandleScope.aapMockResponse(request: HttpRequestData): HttpResponseData {
    val personident = request.receiveBody<AapSakerRequest>().personidentifikator
    val today = LocalDate.now()

    if (personident == AAP_ERROR_PERSONIDENT) {
        return respondError(HttpStatusCode.InternalServerError)
    }
    if (personident == AAP_EMPTY_PERSONIDENT) {
        return respond(AapSakerResponse(emptyList()))
    }

    return respond(
        AapSakerResponse(
            saker = listOf(
                AapSak(
                    sakid = "kelvin-sak-1",
                    statuskode = "SOKNAD_UNDER_BEHANDLING",
                    soknadsdatoer = listOf(today.minusMonths(1)),
                    vedtak = listOf(
                        AapVedtak(
                            vedtaksdato = today.minusMonths(2),
                            perioder = listOf(
                                AapPeriode(
                                    fraOgMedDato = today.minusMonths(2),
                                    tilOgMedDato = today.plusMonths(2),
                                )
                            ),
                        )
                    ),
                    kilde = "KELVIN",
                )
            ),
        )
    )
}

const val AAP_EMPTY_PERSONIDENT = "00000000000"
const val AAP_ERROR_PERSONIDENT = "99999999999"
