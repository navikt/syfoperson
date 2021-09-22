package no.nav.syfo.consumer.pdl

import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.person.api.domain.AktorId
import no.nav.syfo.person.api.domain.Fnr
import no.nav.syfo.util.ALLE_TEMA_HEADERVERDI
import no.nav.syfo.util.TEMA_HEADER
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.*

@Service
class PdlConsumer(
    private val azureAdTokenConsumer: AzureAdV2TokenConsumer,
    private val metric: Metric,
    @Qualifier("restTemplateWithProxy") private val restTemplateProxy: RestTemplate,
    @Value("\${pdl.client.id}") private val pdlClientId: String,
    @Value("\${pdl.url}") private val pdlUrl: String,
) {
    fun aktorId(
        personIdent: Fnr,
        callId: String
    ): AktorId {
        return identer(personIdent.fnr, callId).aktorId()
            ?: throw PdlRequestFailedException("Request to get Ident of Type ${IdentType.AKTORID.name} from PDL Failed")
    }

    fun identer(ident: String, callId: String): PdlHentIdenter? {
        val request = PdlHentIdenterRequest(
            query = getPdlQuery("/pdl/hentIdenter.graphql"),
            variables = PdlHentIdenterRequestVariables(
                ident = ident,
                historikk = false,
                grupper = listOf(
                    IdentType.AKTORID.name,
                    IdentType.FOLKEREGISTERIDENT.name
                )
            )
        )
        val entity = HttpEntity(
            request,
            createRequestHeaders()
        )
        try {
            val pdlReponseEntity = restTemplateProxy.exchange(
                pdlUrl,
                HttpMethod.POST,
                entity,
                PdlIdenterResponse::class.java
            )
            val pdlIdenterReponse = pdlReponseEntity.body!!
            return if (pdlIdenterReponse.errors != null && pdlIdenterReponse.errors.isNotEmpty()) {
                metric.countEvent(CALL_PDL_IDENTER_FAIL)
                pdlIdenterReponse.errors.forEach {
                    LOG.error("Error while requesting Identer from PersonDataLosningen: ${it.errorMessage()}")
                }
                null
            } else {
                metric.countEvent(CALL_PDL_IDENTER_SUCCESS)
                pdlIdenterReponse.data
            }
        } catch (exception: RestClientResponseException) {
            metric.countEvent(CALL_PDL_IDENTER_FAIL)
            LOG.error("Error from PDL with request-url: $pdlUrl", exception)
            throw exception
        }
    }

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
            val pdlPerson = restTemplateProxy.exchange(
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

        const val CALL_PDL_IDENTER_FAIL = "${CALL_PDL_BASE}_identer_fail"
        const val CALL_PDL_IDENTER_SUCCESS = "${CALL_PDL_BASE}_identer_success"
    }
}
