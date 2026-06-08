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
                if (e.response.status == HttpStatusCode.NotFound) {
                    emptyList()
                } else {
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

    companion object {
        private val log = LoggerFactory.getLogger(AaregClient::class.java)

        private const val cacheKeyPrefix = "AAREG_ARBEIDSFORHOLD"
        private const val cacheExpireSeconds = 12 * 60 * 60L // 12 timer

        const val AAREG_ARBEIDSFORHOLD_PATH = "/api/v2/arbeidstaker/arbeidsforhold"
    }
}
