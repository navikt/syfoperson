package no.nav.syfo.client.skjermedepersonerpip

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class SkjermedePersonerPipClient(
    private val azureAdClient: AzureAdClient,
    private val redisStore: RedisStore,
    private val baseUrl: String,
    private val clientId: String,
) {
    private val httpClient = httpClientDefault()

    suspend fun isSkjermet(
        callId: String,
        personIdentNumber: PersonIdentNumber,
        token: String,
    ): Boolean {
        val cacheKey = "$CACHE_SKJERMET_PERSONIDENT_KEY_PREFIX${personIdentNumber.value}"
        val cachedValue: Boolean? = redisStore.getObject(key = cacheKey)
        if (cachedValue != null) {
            return cachedValue
        } else {
            try {
                val oboToken = azureAdClient.getOnBehalfOfToken(
                    scopeClientId = clientId,
                    token = token,
                )?.accessToken ?: throw RuntimeException("Failed to request access to Person: Failed to get OBO token")
                val url = getSkjermedePersonerPipUrl(personIdentNumber)
                val skjermedePersonerResponse: Boolean = httpClient.get(url) {
                    header(io.ktor.http.HttpHeaders.Authorization, bearerHeader(oboToken))
                    header(NAV_CALL_ID_HEADER, callId)
                    header(NAV_CONSUMER_ID_HEADER, NAV_CONSUMER_APP_ID)
                    header(NAV_PERSONIDENTER_HEADER, personIdentNumber.value)
                }.body()

                COUNT_CALL_SKJERMEDE_PERSONER_SKJERMET_SUCCESS.increment()
                redisStore.setObject(
                    expireSeconds = CACHE_SKJERMET_PERSONIDENT_EXPIRE_SECONDS,
                    key = cacheKey,
                    value = skjermedePersonerResponse,
                )
                return skjermedePersonerResponse
            } catch (e: ClosedReceiveChannelException) {
                COUNT_CALL_SKJERMEDE_PERSONER__SKJERMET_FAIL.increment()
                throw RuntimeException(
                    "Caught ClosedReceiveChannelException in SkjermedePersonerPipClient.isSkjermet",
                    e
                )
            } catch (e: ResponseException) {
                log.error(
                    "Error while requesting Response from Skjermede Person {}, {}, {}",
                    StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                    StructuredArguments.keyValue("message", e.message),
                    StructuredArguments.keyValue("callId", callId),
                )
                COUNT_CALL_SKJERMEDE_PERSONER__SKJERMET_FAIL.increment()
                throw e
            }
        }
    }

    private fun getSkjermedePersonerPipUrl(personIdent: PersonIdentNumber): String {
        return "$baseUrl/skjermet?personident=${personIdent.value}"
    }

    companion object {
        const val CACHE_SKJERMET_PERSONIDENT_KEY_PREFIX = "skjermet-personident"
        const val CACHE_SKJERMET_PERSONIDENT_EXPIRE_SECONDS = 12 * 60 * 60L

        private val log = LoggerFactory.getLogger(SkjermedePersonerPipClient::class.java)
    }
}
