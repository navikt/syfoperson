package no.nav.syfo.client.pdl

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class PdlClient(
    private val azureAdClient: AzureAdClient,
    private val redisStore: RedisStore,
    private val baseUrl: String,
    private val clientId: String,
) {
    private val httpClient = httpClientDefault()

    suspend fun hasAdressebeskyttelse(
        callId: String,
        personIdent: PersonIdentNumber,
    ): Boolean? {
        val cacheKey = "$CACHE_ADRESSEBESKYTTELSE_PERSON_KEY_PREFIX${personIdent.value}"
        val cachedValue: Boolean? = redisStore.getObject(cacheKey)
        return if (cachedValue != null) {
            COUNT_CALL_PDL_ADRESSEBESKYTTELSE_CACHE_HIT.increment()
            cachedValue
        } else {
            COUNT_CALL_PDL_ADRESSEBESKYTTELSE_CACHE_MISS.increment()
            val hasAdressebeskyttelse = person(callId = callId, personIdentNumber = personIdent)?.hentPerson?.isKode6Or7
            if (hasAdressebeskyttelse != null) {
                redisStore.setObject(
                    expireSeconds = CACHE_ADRESSEBESKYTTELSE_PERSON_EXPIRE_SECONDS,
                    key = cacheKey,
                    value = hasAdressebeskyttelse,
                )
            }
            hasAdressebeskyttelse
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
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, bearerHeader(systemToken.accessToken))
                header(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
                header(NAV_CALL_ID_HEADER, callId)
                setBody(request)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val pdlResponse = response.body<PdlPersonResponse>()
                    return if (!pdlResponse.errors.isNullOrEmpty()) {
                        COUNT_CALL_PDL_PERSON_FAIL.increment()
                        pdlResponse.errors.forEach {
                            log.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                        }
                        null
                    } else {
                        COUNT_CALL_PDL_PERSON_SUCCESS.increment()
                        pdlResponse.data
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
                "Error while requesting Person from PersonDataLosningen {}, {}, {}",
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
        const val CACHE_ADRESSEBESKYTTELSE_PERSON_KEY_PREFIX = "adressebeskyttelse-person-"
        const val CACHE_ADRESSEBESKYTTELSE_PERSON_EXPIRE_SECONDS = 12 * 60 * 60L

        private val log = LoggerFactory.getLogger(PdlClient::class.java)
    }
}
