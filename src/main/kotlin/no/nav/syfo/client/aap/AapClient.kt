package no.nav.syfo.client.aap

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.NAV_CALL_ID_HEADER
import no.nav.syfo.util.bearerHeader
import org.slf4j.LoggerFactory

class AapClient(
    private val azureAdClient: AzureAdClient,
    private val valkeyStore: ValkeyStore,
    baseUrl: String,
    private val clientId: String,
    private val httpClient: HttpClient = httpClientDefault(),
) {
    private val sakerUrl = "$baseUrl$AAP_SAKER_PATH"

    suspend fun getSaker(
        personident: PersonIdentNumber,
        token: String,
        callId: String,
    ): AapSakerResponse {
        val cacheKey = "$CACHE_KEY_PREFIX-${personident.value}"
        val cachedResponse = valkeyStore.getObject<AapSakerResponse>(cacheKey)

        if (cachedResponse != null) {
            COUNT_CALL_AAP_SAKER_CACHE_HIT.increment()
            return cachedResponse
        }
        COUNT_CALL_AAP_SAKER_CACHE_MISS.increment()

        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = clientId,
            token = token,
        )?.accessToken
            ?: throw RuntimeException(
                "Failed to request response from aap-api-intern: Failed to get OBO token"
            )

        return try {
            val response = httpClient.post(sakerUrl) {
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                header(NAV_CALL_ID_HEADER, callId)
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(AapSakerRequest(personident.value))
            }.body<AapSakerResponse>()

            COUNT_CALL_AAP_SAKER_SUCCESS.increment()
            valkeyStore.setObject(
                expireSeconds = CACHE_EXPIRE_SECONDS,
                key = cacheKey,
                value = response,
            )
            response
        } catch (e: ResponseException) {
            COUNT_CALL_AAP_SAKER_FAIL.increment()
            log.error(
                "Failed to request AAP saker, status=${e.response.status.value}, callId=$callId",
                e,
            )
            throw e
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AapClient::class.java)

        const val AAP_SAKER_PATH = "/syfo/sakerByFnr"
        const val CACHE_KEY_PREFIX = "AAP_SAKER"
        const val CACHE_EXPIRE_SECONDS = 12 * 60 * 60L // 12 hours
    }
}
