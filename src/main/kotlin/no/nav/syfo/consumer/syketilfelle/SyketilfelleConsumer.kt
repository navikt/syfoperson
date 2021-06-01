package no.nav.syfo.consumer.syketilfelle

import no.nav.syfo.consumer.sts.StsConsumer
import no.nav.syfo.consumer.syketilfelle.domain.KOppfolgingstilfellePerson
import no.nav.syfo.metric.Metric
import no.nav.syfo.person.api.domain.AktorId
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@Service
class SyketilfelleConsumer @Inject constructor(
    @Value("\${syfosyketilfelle.url}") private val syketilfelleBaseUrl: String,
    private val metric: Metric,
    private val restTemplate: RestTemplate,
    private val stsConsumer: StsConsumer,
) {

    fun getOppfolgingstilfellePerson(
        aktorId: AktorId,
        callId: String,
    ): KOppfolgingstilfellePerson? {
        try {
            val response = restTemplate.exchange(
                getSyfosyketilfelleUrl(aktorId),
                HttpMethod.GET,
                entity(),
                KOppfolgingstilfellePerson::class.java
            )
            if (response.statusCodeValue == 204) {
                LOG.info("Syketilfelle returned HTTP-${response.statusCodeValue}: No Oppfolgingstilfelle was found for AktorId")
                return null
            }
            val responseBody: KOppfolgingstilfellePerson = response.body!!
            metric.countEvent(CALL_SYFOSYKETILFELLE_PERSON_SUCCESS)
            return responseBody
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Oppfolgingstilfelle for Person from Syfosyketilfelle failed with status ${e.rawStatusCode} and message: ${e.responseBodyAsString}")
            metric.countEvent(CALL_SYFOSYKETILFELLE_PERSON_FAIL)
            throw e
        }
    }

    private fun getSyfosyketilfelleUrl(
        aktorId: AktorId,
    ): String {
        return "$syketilfelleBaseUrl/kafka/oppfolgingstilfelle/beregn/${aktorId.value}"
    }

    private fun entity(): HttpEntity<String> {
        val token = stsConsumer.token()
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        headers[NAV_CALL_ID_HEADER] = createCallId()
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        return HttpEntity<String>(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SyketilfelleConsumer::class.java)

        private const val CALL_SYFOSYKETILFELLE_PERSON_BASE = "call_syfosyketilfelle_person"
        private const val CALL_SYFOSYKETILFELLE_PERSON_FAIL = "${CALL_SYFOSYKETILFELLE_PERSON_BASE}_fail"
        private const val CALL_SYFOSYKETILFELLE_PERSON_SUCCESS = "${CALL_SYFOSYKETILFELLE_PERSON_BASE}_success"
    }
}
