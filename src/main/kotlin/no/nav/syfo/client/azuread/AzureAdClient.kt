package no.nav.syfo.client.azuread

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.syfo.application.api.authentication.getConsumerClientId
import no.nav.syfo.application.api.authentication.getNAVIdentFromToken
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.httpClientProxy
import org.slf4j.LoggerFactory

class AzureAdClient(
    private val azureAppClientId: String,
    private val azureAppClientSecret: String,
    private val azureOpenidConfigTokenEndpoint: String,
    private val redisStore: RedisStore,
) {
    private val httpClient = httpClientProxy()

    suspend fun getOnBehalfOfToken(
        scopeClientId: String,
        token: String,
    ): AzureAdToken? {
        val azp = getConsumerClientId(token)
        val veilederIdent = getNAVIdentFromToken(token)

        val cacheKey = "$veilederIdent-$azp-$scopeClientId"
        val cachedToken: AzureAdToken? = redisStore.getObject(key = cacheKey)
        if (cachedToken?.isExpired() == false) {
            COUNT_CALL_AZUREAD_TOKEN_SYSTEM_CACHE_HIT.increment()
            return cachedToken
        } else {
            val scope = "api://$scopeClientId/.default"
            val azureAdTokenResponse = getAccessToken(
                Parameters.build {
                    append("client_id", azureAppClientId)
                    append("client_secret", azureAppClientSecret)
                    append("client_assertion_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                    append("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                    append("assertion", token)
                    append("scope", scope)
                    append("requested_token_use", "on_behalf_of")
                }
            )

            return azureAdTokenResponse?.let { token ->
                val azureAdToken = token.toAzureAdToken()
                COUNT_CALL_AZUREAD_TOKEN_SYSTEM_CACHE_MISS.increment()
                redisStore.setObject(
                    expireSeconds = token.expires_in,
                    key = cacheKey,
                    value = azureAdToken,
                )
                azureAdToken
            }
        }
    }

    suspend fun getSystemToken(scopeClientId: String): AzureAdToken? {
        val cacheKey = "${CACHE_AZUREAD_TOKEN_SYSTEM_KEY_PREFIX}$scopeClientId"
        val cachedToken = redisStore.getObject<AzureAdToken>(key = cacheKey)
        if (cachedToken?.isExpired() == false) {
            COUNT_CALL_AZUREAD_TOKEN_SYSTEM_CACHE_HIT.increment()
            return cachedToken
        } else {
            val azureAdTokenResponse = getAccessToken(
                Parameters.build {
                    append("client_id", azureAppClientId)
                    append("client_secret", azureAppClientSecret)
                    append("grant_type", "client_credentials")
                    append("scope", "api://$scopeClientId/.default")
                }
            )
            return azureAdTokenResponse?.let { token ->
                val azureAdToken = token.toAzureAdToken()
                COUNT_CALL_AZUREAD_TOKEN_SYSTEM_CACHE_MISS.increment()
                redisStore.setObject(
                    expireSeconds = token.expires_in,
                    key = cacheKey,
                    value = azureAdToken,
                )
                azureAdToken
            }
        }
    }

    private suspend fun getAccessToken(
        formParameters: Parameters,
    ): AzureAdTokenResponse? {
        return try {
            val response: HttpResponse = httpClient.post(azureOpenidConfigTokenEndpoint) {
                accept(ContentType.Application.Json)
                body = FormDataContent(formParameters)
            }
            response.receive<AzureAdTokenResponse>()
        } catch (e: ClientRequestException) {
            handleUnexpectedResponseException(e)
            null
        } catch (e: ServerResponseException) {
            handleUnexpectedResponseException(e)
            null
        }
    }

    private fun handleUnexpectedResponseException(
        responseException: ResponseException,
    ) {
        log.error(
            "Error while requesting AzureAdAccessToken with statusCode=${responseException.response.status.value}",
            responseException
        )
    }

    companion object {
        const val CACHE_AZUREAD_TOKEN_SYSTEM_KEY_PREFIX = "azuread-token-system-"

        private val log = LoggerFactory.getLogger(AzureAdClient::class.java)
    }
}
