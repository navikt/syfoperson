package no.nav.syfo.consumer.dkif

import no.nav.syfo.consumer.sts.StsConsumer
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
    @Value("\${dkif.url}") private val dkifUrl: String,
    private val metric: Metric,
    private val stsConsumer: StsConsumer,
    private val template: RestTemplate
) {
    private val dkifKontaktinfoUrl: String

    init {
        this.dkifKontaktinfoUrl = "$dkifUrl$DKIF_KONTAKTINFO_PATH"
    }

    fun digitalKontaktinfoBolk(ident: Fnr): DigitalKontaktinfoBolk {
        try {
            val response = template.exchange(
                this.dkifKontaktinfoUrl,
                HttpMethod.GET,
                entity(ident),
                DigitalKontaktinfoBolk::class.java
            )

            response.body?.let { digitalKontakinfoBolk ->
                metric.countEvent(CALL_DKIF_SUCCESS)
                return digitalKontakinfoBolk
            } ?: run {
                val errorMessage = "Failed to get Kontaktinfo from DKIF: ReponseBody is null"
                LOG.error(errorMessage)
                throw DKIFRequestFailedException(errorMessage)
            }
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Kontaktinfo from DKIF failed with HTTP-status: ${e.rawStatusCode} and ${e.statusText}")
            metric.countEvent(CALL_DKIF_FAIL)
            throw e
        }
    }

    private fun entity(ident: Fnr): HttpEntity<String> {
        val token: String = stsConsumer.token()
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

        const val DKIF_KONTAKTINFO_PATH = "/api/v1/personer/kontaktinformasjon"

        private const val CALL_DKIF_BASE = "call_dkif"
        const val CALL_DKIF_FAIL = "${CALL_DKIF_BASE}_fail"
        const val CALL_DKIF_SUCCESS = "${CALL_DKIF_BASE}_success"
    }
}
