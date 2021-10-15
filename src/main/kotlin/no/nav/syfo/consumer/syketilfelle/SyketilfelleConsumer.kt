package no.nav.syfo.consumer.syketilfelle

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.metric.Metric
import no.nav.syfo.person.api.domain.AktorId
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@Component
class SyketilfelleConsumer @Inject constructor(
    @Value("\${isproxy.url}") private val isproxyBaseUrl: String,
    @Value("\${isproxy.client.id}") private val isproxyClientId: String,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    private val contextHolder: TokenValidationContextHolder,
    private val metric: Metric,
    private val restTemplate: RestTemplate,
) {
    fun getOppfolgingstilfellePersonArbeidsgiver(
        aktorId: AktorId,
        callId: String,
        virksomhetsnummer: Virksomhetsnummer,
    ): KOppfolgingstilfelle? {
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

        try {
            val url = getSyfosyketilfelleUrl(
                aktorId = aktorId,
                virksomhetsnummer = virksomhetsnummer,
            )
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity(token = oboToken),
                KOppfolgingstilfelle::class.java
            )
            if (response.statusCode == HttpStatus.NO_CONTENT) {
                LOG.info("Syketilfelle returned HTTP-${response.statusCodeValue}: No Oppfolgingstilfelle was found for AktorId")
                return null
            }
            val responseBody: KOppfolgingstilfelle = response.body!!
            metric.countEvent(CALL_SYFOSYKETILFELLE_PERSON_NO_ARBEIDSGIVER_FAIL)
            return responseBody
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Oppfolgingstilfelle for Person and Arbeidsgiver from Isproxy-Syfosyketilfelle failed with status ${e.rawStatusCode} and message: ${e.responseBodyAsString}")
            metric.countEvent(CALL_SYFOSYKETILFELLE_PERSON_NO_ARBEIDSGIVER_SUCCESS)
            throw e
        }
    }

    private fun getSyfosyketilfelleUrl(
        aktorId: AktorId,
        virksomhetsnummer: Virksomhetsnummer,
    ): String {
        return "$isproxyBaseUrl$ISPROXY_SYFOSYKETILFELLE_OPPFOLGINGSTILFELLE_PERSON_PATH/${aktorId.value}/${virksomhetsnummer.value}"
    }

    fun getOppfolgingstilfellePerson(
        aktorId: AktorId,
        callId: String,
    ): KOppfolgingstilfellePerson? {
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

        try {
            val response = restTemplate.exchange(
                getSyfosyketilfelleUrl(aktorId),
                HttpMethod.GET,
                entity(token = oboToken),
                KOppfolgingstilfellePerson::class.java
            )
            if (response.statusCode == HttpStatus.NO_CONTENT) {
                LOG.info("Syketilfelle returned HTTP-${response.statusCodeValue}: No Oppfolgingstilfelle was found for AktorId")
                return null
            }
            val responseBody: KOppfolgingstilfellePerson = response.body!!
            metric.countEvent(CALL_SYFOSYKETILFELLE_PERSON_NO_ARBEIDSGIVER_FAIL)
            return responseBody
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Oppfolgingstilfelle for Person with no Arbeidsgiver from Isproxy-Syfosyketilfelle failed with status ${e.rawStatusCode} and message: ${e.responseBodyAsString}")
            metric.countEvent(CALL_SYFOSYKETILFELLE_PERSON_NO_ARBEIDSGIVER_SUCCESS)
            throw e
        }
    }

    private fun getSyfosyketilfelleUrl(
        aktorId: AktorId,
    ): String {
        return "$isproxyBaseUrl$ISPROXY_SYFOSYKETILFELLE_OPPFOLGINGSTILFELLE_PERSON_PATH/${aktorId.value}/utenarbeidsgiver"
    }

    private fun entity(token: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        headers[NAV_CALL_ID_HEADER] = createCallId()
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        return HttpEntity<String>(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SyketilfelleConsumer::class.java)

        const val ISPROXY_SYFOSYKETILFELLE_PATH = "/api/v1/syfosyketilfelle"
        const val ISPROXY_SYFOSYKETILFELLE_OPPFOLGINGSTILFELLE_PERSON_PATH =
            "$ISPROXY_SYFOSYKETILFELLE_PATH/oppfolgingstilfelle/person"

        private const val CALL_SYFOSYKETILFELLE_PERSON_BASE = "call_syfosyketilfelle_person"
        private const val CALL_SYFOSYKETILFELLE_PERSON_NO_ARBEIDSGIVER_FAIL =
            "${CALL_SYFOSYKETILFELLE_PERSON_BASE}_no_arbeidsgiver_fail"
        private const val CALL_SYFOSYKETILFELLE_PERSON_NO_ARBEIDSGIVER_SUCCESS =
            "${CALL_SYFOSYKETILFELLE_PERSON_BASE}_no_arbeidsgiver_success"
    }
}
