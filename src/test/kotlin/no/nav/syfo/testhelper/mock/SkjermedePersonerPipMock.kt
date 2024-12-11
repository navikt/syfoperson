package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerRequestDTO
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT

suspend fun MockRequestHandleScope.getSkjermedePersonerResponse(request: HttpRequestData): HttpResponseData {
    val skjermedePersonerRequestDTO = request.receiveBody<SkjermedePersonerRequestDTO>()
    val personident = skjermedePersonerRequestDTO.personident

    return when (personident) {
        ARBEIDSTAKER_PERSONIDENT.value -> respond(true)
        ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value -> respond(false)
        ARBEIDSTAKER_ADRESSEBESKYTTET.value -> respond(false)
        else -> error("Unhandled personident")
    }
}
