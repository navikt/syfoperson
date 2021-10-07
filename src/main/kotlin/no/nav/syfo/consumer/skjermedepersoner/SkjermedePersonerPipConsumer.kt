package no.nav.syfo.consumer.skjermedepersoner

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil
import no.nav.syfo.cache.CacheConfig.Companion.EGNEANSATTBYFNR
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@Service
class SkjermedePersonerPipConsumer @Inject constructor(
    @Value("\${skjermedepersonerpip.client.id}") private val skjermedePersonerPipClientId: String,
    @Value("\${skjermedepersonerpip.url}") private val skjermedePersonerPipBaseUrl: String,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    private val contextHolder: TokenValidationContextHolder,
    private val metric: Metric,
    private val restTemplate: RestTemplate
) {
    @Cacheable(cacheNames = [EGNEANSATTBYFNR], key = "#personIdent", condition = "#personIdent != null")
    fun erSkjermet(personIdent: String): Boolean {
        val token = OIDCUtil.tokenFraOIDC(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
        val veilederId = OIDCUtil.getNAVIdentFraOIDC(contextHolder)
            ?: throw RuntimeException("Missing veilederId in OIDC-context")
        val azp = OIDCUtil.getAZPFraOIDC(contextHolder)
            ?: throw RuntimeException("Missing azp in OIDC-context")
        val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
            scopeClientId = skjermedePersonerPipClientId,
            token = token,
            veilederId = veilederId,
            azp = azp,
        )
        try {
            val response = restTemplate.exchange(
                getSkjermedePersonerPipUrl(personIdent),
                HttpMethod.GET,
                entity(token = oboToken),
                Boolean::class.java,
            )
            val skjermedePersonerResponse = response.body!!
            metric.countEvent("call_skjermede_person_pip_success")
            return skjermedePersonerResponse
        } catch (e: RestClientResponseException) {
            metric.countEvent("call_skjermede_person_pip_fail")
            val message = "Call to get response from Skjermede Person failed with status: ${e.rawStatusCode} and message: ${e.responseBodyAsString}"
            LOG.error(message)
            throw e
        }
    }

    private fun getSkjermedePersonerPipUrl(personIdent: String): String {
        return "$skjermedePersonerPipBaseUrl/skjermet?personident=$personIdent"
    }

    private fun entity(token: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers[HttpHeaders.AUTHORIZATION] = bearerHeader(token)
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        headers[NAV_CALL_ID_HEADER] = createCallId()
        return HttpEntity(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SkjermedePersonerPipConsumer::class.java)
    }
}
