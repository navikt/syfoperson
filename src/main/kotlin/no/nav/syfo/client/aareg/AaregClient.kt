package no.nav.syfo.client.aareg

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.NAV_CALL_ID_HEADER
import no.nav.syfo.util.bearerHeader
import org.slf4j.LoggerFactory

class AaregClient(
    private val azureAdClient: AzureAdClient,
    private val valkeyStore: ValkeyStore,
    baseUrl: String,
    private val clientId: String,
    private val httpClient: HttpClient = httpClientDefault(),
) {

    private val arbeidsforholdUrl: String = "$baseUrl$AAREG_ARBEIDSFORHOLD_PATH"

    /**
     * Henter arbeidsforhold for en person fra Aareg (Arbeidsgiver- og arbeidstakerregisteret).
     * Resultatene caches i Valkey i 12 timer for å redusere antall kall til Aareg.
     *
     * Veileder trenger tilgang til AD-gruppen `0000-GA-Aa-register-Lese` for å kunne gjøre oppslaget.
     *
     * @param personident Fødselsnummeret til personen det gjøres oppslag på.
     * @param token OBO-token for den innloggede veilederen, brukes til å hente et nytt OBO-token mot Aareg.
     * @param callId Unik ID for den innkommende forespørselen, brukes for logging og sporing.
     * @return Liste over [ArbeidsforholdResponse] for personen. Returnerer en tom liste dersom personen ikke ble funnet (404).
     * @throws ResponseException Dersom Aareg returnerer 403 Forbidden eller en annen feilkode.
     * @throws RuntimeException Dersom OBO-token ikke kan hentes.
     */
    suspend fun getArbeidsforhold(
        personident: PersonIdentNumber,
        token: String,
        callId: String,
    ): List<ArbeidsforholdResponse> {
        val cacheKey = "$cacheKeyPrefix-${personident.value}"
        val cachedArbeidsforholdResponse: List<ArbeidsforholdResponse>? = valkeyStore.getListObject(cacheKey)

        return if (cachedArbeidsforholdResponse != null) {
            COUNT_CALL_AAREG_ARBEIDSFORHOLD_CACHE_HIT.increment()
            cachedArbeidsforholdResponse
        } else {
            COUNT_CALL_AAREG_ARBEIDSFORHOLD_CACHE_MISS.increment()

            val oboToken = azureAdClient.getOnBehalfOfToken(
                scopeClientId = clientId,
                token = token,
            )?.accessToken ?: throw RuntimeException("Failed to request response from Aareg: Failed to get OBO token")

            val requestDTO = ArbeidsforholdRequest(personident.value)
            try {
                val response: List<ArbeidsforholdResponse> = httpClient.post(arbeidsforholdUrl) {
                    header(HttpHeaders.Authorization, bearerHeader(oboToken))
                    header(NAV_CALL_ID_HEADER, callId)
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(requestDTO)
                }.body()

                COUNT_CALL_AAREG_ARBEIDSFORHOLD_SUCCESS.increment()
                valkeyStore.setObject(
                    expireSeconds = cacheExpireSeconds,
                    key = cacheKey,
                    value = response,
                )
                response
            } catch (e: ResponseException) {
                when (e.response.status) {
                    HttpStatusCode.NotFound -> {
                        emptyList()
                    }
                    HttpStatusCode.Forbidden -> {
                        log.warn(
                            """
                               Access denied while requesting response from Aareg:
                               status: ${e.response.status.value}
                               callId: $callId
                               message: ${e.message}
                            """.trimIndent()
                        )
                        throw e
                    }
                    else -> {
                        log.error(
                            """
                               Error while requesting response from Aareg:
                               status: ${e.response.status.value}
                               callId: $callId
                               message: ${e.message}
                            """.trimIndent()
                        )
                        COUNT_CALL_AAREG_ARBEIDSFORHOLD_FAIL.increment()
                        throw e
                    }
                }
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AaregClient::class.java)

        private const val cacheKeyPrefix = "AAREG_ARBEIDSFORHOLD"
        private const val cacheExpireSeconds = 12 * 60 * 60L // 12 timer

        const val AAREG_ARBEIDSFORHOLD_PATH = "/api/v2/arbeidstaker/arbeidsforhold"
    }
}
