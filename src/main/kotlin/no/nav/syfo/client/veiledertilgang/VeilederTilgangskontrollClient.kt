package no.nav.syfo.client.veiledertilgang

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class VeilederTilgangskontrollClient(
    private val azureAdClient: AzureAdClient,
    baseUrl: String,
    private val clientId: String,
) {
    private val httpClient = httpClientDefault()

    private val tilgangskontrollPersonUrl = "$baseUrl$TILGANGSKONTROLL_PERSON_PATH"
    private val tilgangskontrollPersonListUrl = "$baseUrl$TILGANGSKONTROLL_PERSON_LIST_PATH"

    suspend fun hasAccess(
        callId: String,
        personIdentNumber: PersonIdentNumber,
        token: String,
    ): Boolean {
        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = clientId,
            token = token,
        )?.accessToken ?: throw RuntimeException("Failed to request access to Person: Failed to get OBO token")

        return try {
            val response: HttpResponse = httpClient.get(tilgangskontrollPersonUrl) {
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                header(NAV_PERSONIDENT_HEADER, personIdentNumber.value)
                header(NAV_CALL_ID_HEADER, callId)
                accept(ContentType.Application.Json)
            }
            COUNT_CALL_TILGANGSKONTROLL_PERSON_SUCCESS.increment()
            response.receive<Tilgang>().harTilgang
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Forbidden) {
                COUNT_CALL_TILGANGSKONTROLL_PERSON_FORBIDDEN.increment()
            } else {
                handleUnexpectedResponseException(e.response, callId)
            }
            false
        } catch (e: ServerResponseException) {
            handleUnexpectedResponseException(e.response, callId)
            false
        }
    }

    suspend fun hasVeilederAccessToPersonList(
        callId: String,
        personIdentNumberList: List<PersonIdentNumber>,
        token: String,
    ): List<PersonIdentNumber> {
        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = clientId,
            token = token,
        )?.accessToken ?: throw RuntimeException("Failed to request access to Person: Failed to get OBO token")

        return try {
            val requestBody = personIdentNumberList.map { it.value }

            val response: HttpResponse = httpClient.post(tilgangskontrollPersonListUrl) {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                header(NAV_CALL_ID_HEADER, callId)
                body = requestBody
            }
            COUNT_CALL_TILGANGSKONTROLL_PERSON_SUCCESS.increment()
            response.receive<List<String>>().map {
                PersonIdentNumber(it)
            }
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.Forbidden) {
                COUNT_CALL_TILGANGSKONTROLL_PERSON_FORBIDDEN.increment()
            } else {
                log.error(
                    "Error while requesting access to person from syfo-tilgangskontroll with {}, {}, {}",
                    StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                    StructuredArguments.keyValue("message", e.message),
                    callIdArgument(callId),
                )
                COUNT_CALL_TILGANGSKONTROLL_PERSON_FAIL.increment()
            }
            emptyList()
        }
    }

    private fun handleUnexpectedResponseException(
        response: HttpResponse,
        callId: String,
    ) {
        log.error(
            "Error while requesting access to person from syfo-tilgangskontroll with {}, {}",
            StructuredArguments.keyValue("statusCode", response.status.value.toString()),
            callIdArgument(callId)
        )
        COUNT_CALL_TILGANGSKONTROLL_PERSON_FAIL.increment()
    }

    companion object {
        private val log = LoggerFactory.getLogger(VeilederTilgangskontrollClient::class.java)

        const val TILGANGSKONTROLL_COMMON_PATH = "/syfo-tilgangskontroll/api/tilgang/navident"
        const val TILGANGSKONTROLL_PERSON_PATH = "$TILGANGSKONTROLL_COMMON_PATH/person"
        const val TILGANGSKONTROLL_PERSON_LIST_PATH = "$TILGANGSKONTROLL_COMMON_PATH/brukere"
    }
}
