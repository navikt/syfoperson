package no.nav.syfo.pdl

import no.nav.syfo.cache.CacheConfig.Companion.PERSONBYFNR
import no.nav.syfo.controller.domain.Fnr
import no.nav.syfo.metric.Metric
import no.nav.syfo.sts.StsConsumer
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class PdlConsumer(
        private val metric: Metric,
        @Value("\${pdl.url}") private val pdlUrl: String,
        private val stsConsumer: StsConsumer,
        private val restTemplate: RestTemplate
) {
    @Cacheable(cacheNames = [PERSONBYFNR], key = "#fnr", condition = "#fnr != null")
    fun person(fnr: Fnr): PdlHentPerson? {
        metric.countEvent("call_pdl")

        val query = this::class.java.getResource("/pdl/hentPerson.graphql").readText().replace("[\n\r]", "")
        val entity = createRequestEntity(PdlRequest(query, Variables(fnr.fnr)))
        try {
            val pdlPerson = restTemplate.exchange<PdlPersonResponse>(
                    pdlUrl,
                    HttpMethod.POST,
                    entity,
                    object : ParameterizedTypeReference<PdlPersonResponse>() {}
            )
            metric.countEvent("call_pdl_success")
            return pdlPerson.body!!.data
        } catch (exception: RestClientException) {
            metric.countEvent("call_pdl_fail")
            LOG.error("Error from PDL with request-url: $pdlUrl", exception)
            throw exception
        }
    }

    private fun createRequestEntity(request: PdlRequest): HttpEntity<PdlRequest> {
        val stsToken: String = stsConsumer.token()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
        headers.set(AUTHORIZATION, bearerHeader(stsToken))
        headers.set(NAV_CONSUMER_TOKEN_HEADER, bearerHeader(stsToken))
        return HttpEntity(request, headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlConsumer::class.java)
    }
}
