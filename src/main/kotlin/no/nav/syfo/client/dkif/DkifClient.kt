package no.nav.syfo.client.dkif

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class DkifClient(
    private val azureAdClient: AzureAdClient,
    baseUrl: String,
    private val clientId: String,
) {
    private val httpClient = httpClientDefault()

    private val dkifKontaktinfoUrl: String = "$baseUrl$ISPROXY_DKIF_KONTAKTINFORMASJON_PATH"

    suspend fun digitalKontaktinfo(
        callId: String,
        personIdentNumber: PersonIdentNumber,
        token: String,
    ): DigitalKontaktinfo {
        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = clientId,
            token = token,
        )?.accessToken ?: throw RuntimeException("Failed to request access to Person: Failed to get OBO token")

        val digitalKontaktinfoBolk = digitalKontaktinfoBolk(
            callId = callId,
            ident = personIdentNumber,
            oboToken = oboToken,
        )
        val kontaktinfo = digitalKontaktinfoBolk.kontaktinfo?.get(personIdentNumber.value)
        val feil = digitalKontaktinfoBolk.feil?.get(personIdentNumber.value)
        when {
            kontaktinfo != null -> {
                return kontaktinfo
            }
            feil != null -> {
                if (feil.melding == "Ingen kontaktinformasjon er registrert på personen") {
                    return DigitalKontaktinfo(
                        kanVarsles = false,
                        personident = personIdentNumber.value
                    )
                } else {
                    throw DKIFRequestFailedException(feil.melding)
                }
            }
            else -> {
                throw DKIFRequestFailedException("Kontaktinfo is null")
            }
        }
    }

    private suspend fun digitalKontaktinfoBolk(
        callId: String,
        ident: PersonIdentNumber,
        oboToken: String,
    ): DigitalKontaktinfoBolk {
        try {
            val response: DigitalKontaktinfoBolk? = httpClient.get(this.dkifKontaktinfoUrl) {
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                header(NAV_CALL_ID_HEADER, callId)
                header(NAV_CONSUMER_ID_HEADER, NAV_CONSUMER_APP_ID)
                header(NAV_PERSONIDENTER_HEADER, ident.value)
                accept(ContentType.Application.Json)
            }

            response?.let { digitalKontakinfoBolk ->
                COUNT_CALL_DKIF_KONTAKTINFORMASJON_SUCCESS.increment()
                return digitalKontakinfoBolk
            } ?: run {
                val errorMessage = "Failed to get Kontaktinfo from Isproxy-DKIF: ReponseBody is null"
                log.error(errorMessage)
                throw DKIFRequestFailedException(errorMessage)
            }
        } catch (e: ResponseException) {
            log.error(
                "Error while requesting Response from Ereg {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                StructuredArguments.keyValue("callId", callId),
            )
            COUNT_CALL_DKIF_KONTAKTINFORMASJON_FAIL.increment()
            throw e
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DkifClient::class.java)

        const val ISPROXY_DKIF_KONTAKTINFORMASJON_PATH = "/api/v1/dkif/kontaktinformasjon"
    }
}
