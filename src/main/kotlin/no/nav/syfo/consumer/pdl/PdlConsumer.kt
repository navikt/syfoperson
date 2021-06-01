package no.nav.syfo.consumer.pdl

import no.nav.syfo.consumer.sts.StsConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.person.api.domain.AktorId
import no.nav.syfo.person.api.domain.Fnr
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.*

@Service
class PdlConsumer(
    private val metric: Metric,
    @Value("\${pdl.url}") private val pdlUrl: String,
    private val stsConsumer: StsConsumer,
    private val restTemplate: RestTemplate
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
            val pdlReponseEntity = restTemplate.exchange(
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

    fun isKode6Or7(fnr: Fnr): Boolean {
        return person(fnr)?.isKode6Or7() ?: throw PdlRequestFailedException()
    }

    private fun getPdlQuery(queryFilePath: String): String {
        return this::class.java.getResource(queryFilePath)
            .readText()
            .replace("[\n\r]", "")
    }

    private fun createRequestHeaders(): HttpHeaders {
        val stsToken: String = stsConsumer.token()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
        headers.set(NAV_CONSUMER_TOKEN_HEADER, bearerHeader(stsToken))
        headers.setBearerAuth(stsToken)
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
