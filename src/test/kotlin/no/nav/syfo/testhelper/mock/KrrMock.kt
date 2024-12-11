package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.client.krr.DigitalKontaktinfo
import no.nav.syfo.client.krr.DigitalKontaktinfoBolk
import no.nav.syfo.client.krr.DigitalKontaktinfoBolkRequestBody
import no.nav.syfo.testhelper.UserConstants

fun digitalKontaktinfoBolkKanVarslesTrue(personIdentNumber: String) = DigitalKontaktinfoBolk(
    personer = mapOf(
        personIdentNumber to DigitalKontaktinfo(
            epostadresse = UserConstants.PERSON_EMAIL,
            kanVarsles = true,
            reservert = false,
            mobiltelefonnummer = UserConstants.PERSON_TLF,
            aktiv = true,
            personident = personIdentNumber,
        )
    ),
)

suspend fun MockRequestHandleScope.krrMockResponse(request: HttpRequestData): HttpResponseData {
    val krrRequestBody = request.receiveBody<DigitalKontaktinfoBolkRequestBody>()
    return respond(
        digitalKontaktinfoBolkKanVarslesTrue(
            personIdentNumber = krrRequestBody.personidenter.first(),
        )
    )
}
