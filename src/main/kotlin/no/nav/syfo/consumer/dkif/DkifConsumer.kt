package no.nav.syfo.consumer.dkif

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.person.api.domain.Fnr
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class DkifConsumer(
    @Value("\${isproxy.url}") private val isproxyBaseUrl: String,
    @Value("\${isproxy.client.id}") private val isproxyClientId: String,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    private val contextHolder: TokenValidationContextHolder,
    private val metric: Metric,
    private val restTemplate: RestTemplate,
) {
    private val dkifKontaktinfoUrl: String = "$isproxyBaseUrl$ISPROXY_DKIF_KONTAKTINFORMASJON_PATH"

    fun digitalKontaktinfo(ident: Fnr): DigitalKontaktinfo {
        val digitalKontaktinfoBolk = digitalKontaktinfoBolk(ident = ident)
        val kontaktinfo = digitalKontaktinfoBolk.kontaktinfo?.get(ident.fnr)
        val feil = digitalKontaktinfoBolk.feil?.get(ident.fnr)
        when {
            kontaktinfo != null -> {
                return kontaktinfo
            }
            feil != null -> {
                if (feil.melding == "Ingen kontaktinformasjon er registrert på personen") {
                    return DigitalKontaktinfo(
                        kanVarsles = false,
                        personident = ident.fnr
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

    fun digitalKontaktinfoBolk(ident: Fnr): DigitalKontaktinfoBolk {
        val token = OIDCUtil.tokenFraOIDC(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
        val veilederId = OIDCUtil.getNAVIdentFraOIDC(contextHolder)
            ?: throw RuntimeException("Missing veilederId in OIDC-context")
        val azp = OIDCUtil.getAZPFraOIDC(contextHolder)
            ?: throw RuntimeException("Missing azp in OIDC-context")
        val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
            scopeClientId = isproxyClientId,
            token = token,
            veilederId = veilederId,
            azp = azp,
        )
        val requestEntity = entity(
            ident = ident,
            token = oboToken,
        )
        try {
            val response = restTemplate.exchange(
                this.dkifKontaktinfoUrl,
                HttpMethod.GET,
                requestEntity,
                DigitalKontaktinfoBolk::class.java
            )

            response.body?.let { digitalKontakinfoBolk ->
                metric.countEvent(CALL_DKIF_SUCCESS)
                return digitalKontakinfoBolk
            } ?: run {
                val errorMessage = "Failed to get Kontaktinfo from Isproxy-DKIF: ReponseBody is null"
                LOG.error(errorMessage)
                throw DKIFRequestFailedException(errorMessage)
            }
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Kontaktinfo from DKIF failed with HTTP-status: ${e.rawStatusCode} and ${e.statusText}")
            metric.countEvent(CALL_DKIF_FAIL)
            throw e
        }
    }

    private fun entity(
        ident: Fnr,
        token: String,
    ): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers[HttpHeaders.AUTHORIZATION] = bearerHeader(token)
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        headers[NAV_CALL_ID_HEADER] = createCallId()
        headers[NAV_PERSONIDENTER_HEADER] = ident.fnr
        return HttpEntity(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DkifConsumer::class.java)

        const val ISPROXY_DKIF_KONTAKTINFORMASJON_PATH = "/api/v1/dkif/kontaktinformasjon"

        private const val CALL_DKIF_BASE = "call_dkif"
        const val CALL_DKIF_FAIL = "${CALL_DKIF_BASE}_fail"
        const val CALL_DKIF_SUCCESS = "${CALL_DKIF_BASE}_success"
    }
}
