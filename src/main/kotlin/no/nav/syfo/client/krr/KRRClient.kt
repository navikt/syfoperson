package no.nav.syfo.client.krr

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.NAV_CALL_ID_HEADER
import no.nav.syfo.util.bearerHeader
import org.slf4j.LoggerFactory

class KRRClient(
    private val azureAdClient: AzureAdClient,
    private val redisStore: RedisStore,
    baseUrl: String,
    private val clientId: String,
    private val httpClient: HttpClient = httpClientDefault(),
) {

    private val krrKontaktinfoUrl: String = "$baseUrl$KRR_KONTAKTINFORMASJON_BOLK_PATH"

    suspend fun digitalKontaktinfo(
        callId: String,
        personIdentNumber: PersonIdentNumber,
        token: String,
    ): DigitalKontaktinfo {
        val cacheKey = "$cacheKeyPrefix-${personIdentNumber.value}"
        val cachedKontaktinfo: DigitalKontaktinfo? = redisStore.getObject(cacheKey)
        return if (cachedKontaktinfo != null) {
            COUNT_CALL_KRR_KONTAKTINFORMASJON_CACHE_HIT.increment()
            cachedKontaktinfo
        } else {
            COUNT_CALL_KRR_KONTAKTINFORMASJON_CACHE_MISS.increment()
            val oboToken = azureAdClient.getOnBehalfOfToken(
                scopeClientId = clientId,
                token = token,
            )?.accessToken ?: throw RuntimeException("Failed to request get response from KRR: Failed to get OBO token")

            val digitalKontaktinfoBolk = digitalKontaktinfoBolk(
                callId = callId,
                personIdentList = listOf(personIdentNumber),
                oboToken = oboToken,
            )
            val kontaktinfo = digitalKontaktinfoBolk.personer?.get(personIdentNumber.value)
            val feil = digitalKontaktinfoBolk.feil?.get(personIdentNumber.value)
            when {
                kontaktinfo != null -> {
                    redisStore.setObject(
                        expireSeconds = cacheExpireSeconds,
                        key = cacheKey,
                        value = kontaktinfo,
                    )
                    kontaktinfo
                }

                feil != null -> {
                    throw KRRRequestFailedException(feil)
                }

                else -> {
                    throw KRRRequestFailedException("Kontaktinfo is null")
                }
            }
        }
    }

    private suspend fun digitalKontaktinfoBolk(
        callId: String,
        personIdentList: List<PersonIdentNumber>,
        oboToken: String,
    ): DigitalKontaktinfoBolk {
        val request = DigitalKontaktinfoBolkRequestBody(
            personidenter = personIdentList.map { it.value }
        )
        try {
            val response: DigitalKontaktinfoBolk? = httpClient.post(this.krrKontaktinfoUrl) {
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                header(NAV_CALL_ID_HEADER, callId)
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            response?.let { digitalKontakinfoBolk ->
                COUNT_CALL_KRR_KONTAKTINFORMASJON_SUCCESS.increment()
                return digitalKontakinfoBolk
            } ?: run {
                val errorMessage = "Failed to get Kontaktinfo from KRR: ReponseBody is null"
                log.error(errorMessage)
                throw KRRRequestFailedException(errorMessage)
            }
        } catch (e: ClosedReceiveChannelException) {
            COUNT_CALL_KRR_KONTAKTINFORMASJON_FAIL.increment()
            throw RuntimeException("Caught ClosedReceiveChannelException in DkifClient.digitalKontaktinfoBolk", e)
        } catch (e: ResponseException) {
            log.error(
                "Error while requesting Response from KRR {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                StructuredArguments.keyValue("callId", callId),
            )
            COUNT_CALL_KRR_KONTAKTINFORMASJON_FAIL.increment()
            throw e
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(KRRClient::class.java)
        private val cacheKeyPrefix = "KRR_KONTAKTINFO"
        private val cacheExpireSeconds = 12 * 60 * 60L

        const val KRR_KONTAKTINFORMASJON_BOLK_PATH = "/rest/v1/personer"
    }
}
