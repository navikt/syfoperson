package no.nav.syfo.client.pdl

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class PdlClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
    private val clientId: String,
) {
    private val httpClient = httpClientDefault()

    suspend fun aktorId(
        callId: String,
        personIdentNumber: PersonIdentNumber,
    ): AktorId {
        return identer(
            callId = callId,
            ident = personIdentNumber.value,
        ).aktorId()
            ?: throw PdlRequestFailedException("Request to get Ident of Type ${IdentType.AKTORID.name} from PDL Failed")
    }

    private suspend fun identer(
        callId: String,
        ident: String,
    ): PdlHentIdenter? {
        val request = PdlHentIdenterRequest(
            query = getPdlQuery("/pdl/hentIdenter.graphql"),
            variables = PdlHentIdenterRequestVariables(
                ident = ident,
                historikk = false,
                grupper = listOf(
                    IdentType.AKTORID.name,
                    IdentType.FOLKEREGISTERIDENT.name
                )
            )
        )
        val systemToken = azureAdClient.getSystemToken(
            scopeClientId = clientId,
        ) ?: throw RuntimeException("Failed to send request to PDL: No token was found")
        try {
            val response: HttpResponse = httpClient.post(baseUrl) {
                body = request
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, bearerHeader(systemToken.accessToken))
                header(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
                header(NAV_CALL_ID_HEADER, callId)
                header(IDENTER_HEADER, IDENTER_HEADER)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val pdlReponse = response.receive<PdlHentIdenterResponse>()
                    return if (!pdlReponse.errors.isNullOrEmpty()) {
                        COUNT_CALL_PDL_IDENTER_FAIL.increment()
                        pdlReponse.errors.forEach {
                            log.error("Error while requesting Identer from PersonDataLosningen: ${it.errorMessage()}")
                        }
                        null
                    } else {
                        COUNT_CALL_PDL_IDENTER_SUCCESS.increment()
                        pdlReponse.data
                    }
                }
                else -> {
                    COUNT_CALL_PDL_IDENTER_FAIL.increment()
                    log.error("Request with url: $baseUrl failed with reponse code ${response.status.value}")
                    return null
                }
            }
        } catch (e: ResponseException) {
            COUNT_CALL_PDL_IDENTER_FAIL.increment()
            log.error(
                "Error while requesting Identer from PersonDataLosningen {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                callIdArgument(callId),
            )
            throw e
        }
    }

    suspend fun person(
        callId: String,
        personIdentNumber: PersonIdentNumber,
    ): PdlHentPerson? {
        val request = PdlRequest(
            query = getPdlQuery("/pdl/hentPerson.graphql"),
            variables = Variables(personIdentNumber.value),
        )
        val systemToken = azureAdClient.getSystemToken(
            scopeClientId = clientId,
        ) ?: throw RuntimeException("Failed to send request to PDL: No token was found")
        try {

            val response: HttpResponse = httpClient.post(baseUrl) {
                body = request
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, bearerHeader(systemToken.accessToken))
                header(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
                header(NAV_CALL_ID_HEADER, callId)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val pdlReponse = response.receive<PdlPersonResponse>()
                    return if (!pdlReponse.errors.isNullOrEmpty()) {
                        COUNT_CALL_PDL_PERSON_FAIL.increment()
                        pdlReponse.errors.forEach {
                            log.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                        }
                        null
                    } else {
                        COUNT_CALL_PDL_PERSON_SUCCESS.increment()
                        pdlReponse.data
                    }
                }
                else -> {
                    COUNT_CALL_PDL_PERSON_FAIL.increment()
                    log.error("Request with url: $baseUrl failed with reponse code ${response.status.value}")
                    return null
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            COUNT_CALL_PDL_PERSON_FAIL.increment()
            throw RuntimeException("Caught ClosedReceiveChannelException in PdlClient.person", e)
        } catch (e: ResponseException) {
            COUNT_CALL_PDL_PERSON_FAIL.increment()
            log.error(
                "Error while requesting Identer from PersonDataLosningen {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                callIdArgument(callId),
            )
            throw e
        }
    }

    private fun getPdlQuery(queryFilePath: String): String {
        return this::class.java.getResource(queryFilePath)
            .readText()
            .replace("[\n\r]", "")
    }

    companion object {
        private val log = LoggerFactory.getLogger(PdlClient::class.java)

        const val IDENTER_HEADER = "identer"
    }
}
