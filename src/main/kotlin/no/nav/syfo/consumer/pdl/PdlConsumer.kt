package no.nav.syfo.consumer.pdl

import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.person.api.domain.Fnr
import no.nav.syfo.util.ALLE_TEMA_HEADERVERDI
import no.nav.syfo.util.TEMA_HEADER
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class PdlConsumer(
    private val azureAdTokenConsumer: AzureAdV2TokenConsumer,
    private val metric: Metric,
    private val restTemplate: RestTemplate,
    @Value("\${pdl.client.id}") private val pdlClientId: String,
    @Value("\${pdl.url}") private val pdlUrl: String,
) {
    fun person(fnr: Fnr): PdlHentPerson? {
        val request = PdlRequest(
            getPdlQuery("/pdl/hentPerson.graphql"),
            Variables(fnr.fnr),
        )
        val entity = HttpEntity(
            request,
            createRequestHeaders(),
        )
        try {
            val pdlPerson = restTemplate.exchange(
                pdlUrl,
                HttpMethod.POST,
                entity,
                PdlPersonResponse::class.java
            )

            val pdlPersonResponse = pdlPerson.body!!
            return if (pdlPersonResponse.errors != null && pdlPersonResponse.errors.isNotEmpty()) {
                metric.countEvent(CALL_PDL_PERSON_FAIL)
                pdlPersonResponse.errors.forEach {
                    LOG.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                }
                null
            } else {
                metric.countEvent(CALL_PDL_PERSON_SUCCESS)
                pdlPersonResponse.data
            }
        } catch (exception: RestClientException) {
            metric.countEvent(CALL_PDL_PERSON_FAIL)
            LOG.error("Error from PDL with request-url: $pdlUrl", exception)
            throw exception
        }
    }

    private fun getPdlQuery(queryFilePath: String): String {
        return this::class.java.getResource(queryFilePath)
            .readText()
            .replace("[\n\r]", "")
    }

    private fun createRequestHeaders(): HttpHeaders {
        val azureADSystemToken = azureAdTokenConsumer.getSystemToken(
            scopeClientId = pdlClientId,
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
        headers.setBearerAuth(azureADSystemToken)
        return headers
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlConsumer::class.java)

        private const val CALL_PDL_BASE = "call_pdl"
        const val CALL_PDL_PERSON_FAIL = "${CALL_PDL_BASE}_fail"
        const val CALL_PDL_PERSON_SUCCESS = "${CALL_PDL_BASE}_success"
    }
}
