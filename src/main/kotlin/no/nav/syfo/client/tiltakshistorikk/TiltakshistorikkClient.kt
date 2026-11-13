package no.nav.syfo.client.tiltakshistorikk

import com.fasterxml.jackson.databind.JsonNode
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
import no.nav.syfo.util.configuredJacksonMapper
import org.slf4j.LoggerFactory

class TiltakshistorikkClient(
    private val azureAdClient: AzureAdClient,
    private val valkeyStore: ValkeyStore,
    baseUrl: String,
    private val clientId: String,
    private val httpClient: HttpClient = httpClientDefault(),
) {
    private val historikkUrl = "$baseUrl$TILTAKSHISTORIKK_PATH"

    suspend fun getHistorikk(
        identer: List<PersonIdentNumber>,
        callId: String,
    ): TiltakshistorikkResponse {
        val identValues = identer.map { it.value }.distinct().sorted()
        val cacheKey = "$CACHE_KEY_PREFIX-${identValues.joinToString(",")}"
        val cachedResponse = valkeyStore.getObject<TiltakshistorikkResponse>(cacheKey)

        if (cachedResponse != null) {
            COUNT_CALL_TILTAKSHISTORIKK_CACHE_HIT.increment()
            return cachedResponse
        }
        COUNT_CALL_TILTAKSHISTORIKK_CACHE_MISS.increment()

        val systemToken = azureAdClient.getSystemToken(
            scopeClientId = clientId,
        )?.accessToken
            ?: throw RuntimeException("Failed to request tiltakshistorikk: Failed to get system token")

        return try {
            val rawResponse = httpClient.post(historikkUrl) {
                header(HttpHeaders.Authorization, bearerHeader(systemToken))
                header(NAV_CALL_ID_HEADER, callId)
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(TiltakshistorikkRequest(identer = identValues))
            }.body<TiltakshistorikkRawResponse>()

            val response = TiltakshistorikkResponse(
                historikk = rawResponse.historikk.mapNotNull { it.toDeltakelseOrNull(callId) },
            )
            COUNT_CALL_TILTAKSHISTORIKK_SUCCESS.increment()
            valkeyStore.setObject(
                expireSeconds = CACHE_EXPIRE_SECONDS,
                key = cacheKey,
                value = response,
            )
            response
        } catch (e: ResponseException) {
            COUNT_CALL_TILTAKSHISTORIKK_FAIL.increment()
            log.error(
                "Failed to request tiltakshistorikk, status=${e.response.status.value}, callId=$callId",
                e,
            )
            throw e
        }
    }

    private fun JsonNode.toDeltakelseOrNull(callId: String): TiltakshistorikkDeltakelse? =
        try {
            mapper.treeToValue(this, TiltakshistorikkDeltakelse::class.java)
        } catch (e: Exception) {
            COUNT_TILTAKSHISTORIKK_SKIPPED_ELEMENT.increment()
            // Logger bare type og feilklasse, siden elementet inneholder persondata
            log.warn(
                "Skipping element from tiltakshistorikk that could not be parsed, " +
                    "type=${get("type")?.asText()}, error=${e.javaClass.simpleName}, callId=$callId",
            )
            null
        }

    companion object {
        private val log = LoggerFactory.getLogger(TiltakshistorikkClient::class.java)
        private val mapper = configuredJacksonMapper()

        const val TILTAKSHISTORIKK_PATH = "/api/v1/historikk"
        const val CACHE_KEY_PREFIX = "TILTAKSHISTORIKK"
        const val CACHE_EXPIRE_SECONDS = 60 * 60L // 1 hour
    }
}
