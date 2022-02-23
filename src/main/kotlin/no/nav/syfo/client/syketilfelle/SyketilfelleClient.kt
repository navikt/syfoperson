package no.nav.syfo.client.syketilfelle

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class SyketilfelleClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
    private val clientId: String,
) {
    private val httpClient = httpClientDefault()

    suspend fun getOppfolgingstilfellePersonArbeidsgiver(
        aktorId: AktorId,
        callId: String,
        token: String,
        virksomhetsnummer: Virksomhetsnummer,
    ): KOppfolgingstilfelleDTO? {
        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = clientId,
            token = token,
        )?.accessToken ?: throw RuntimeException("Failed to request access to Person: Failed to get OBO token")

        try {
            val url = getSyfosyketilfelleUrl(
                aktorId = aktorId,
                virksomhetsnummer = virksomhetsnummer,
            )
            val response: HttpResponse = httpClient.get(url) {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                header(NAV_CALL_ID_HEADER, callId)
                header(NAV_CONSUMER_ID_HEADER, NAV_CONSUMER_APP_ID)
            }

            if (response.status == HttpStatusCode.NoContent) {
                log.info("Syketilfelle returned HTTP-${response.status.value}: No Oppfolgingstilfelle was found for AktorId")
                return null
            }
            val responseBody: KOppfolgingstilfelleDTO = response.receive()
            COUNT_CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_SUCCESS.increment()
            return responseBody
        } catch (e: ServerResponseException) {
            log.error(
                "Error while requesting Oppfolgingstilfelle for Person and Arbeidsgiver from Isproxy-Syfosyketilfelle with {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                callIdArgument(callId),
            )
            COUNT_CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_FAIL.increment()
            throw e
        }
    }

    private fun getSyfosyketilfelleUrl(
        aktorId: AktorId,
        virksomhetsnummer: Virksomhetsnummer,
    ): String {
        return "$baseUrl$ISPROXY_SYFOSYKETILFELLE_OPPFOLGINGSTILFELLE_PERSON_PATH/${aktorId.value}/${virksomhetsnummer.value}"
    }

    companion object {
        private val log = LoggerFactory.getLogger(SyketilfelleClient::class.java)

        const val ISPROXY_SYFOSYKETILFELLE_PATH = "/api/v1/syfosyketilfelle"
        const val ISPROXY_SYFOSYKETILFELLE_OPPFOLGINGSTILFELLE_PERSON_PATH =
            "$ISPROXY_SYFOSYKETILFELLE_PATH/oppfolgingstilfelle/person"
    }
}
