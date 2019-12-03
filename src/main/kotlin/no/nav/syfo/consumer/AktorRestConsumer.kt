package no.nav.syfo.consumer

import no.nav.syfo.CALL_ID
import no.nav.syfo.consumer.domain.AktorResponse
import no.nav.syfo.token.TokenConsumer
import no.nav.syfo.util.EnvironmentUtil
import no.nav.syfo.util.MDCOperations.getFromMDC
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class AktorRestConsumer(private val tokenConsumer: TokenConsumer,
                        private val restTemplate: RestTemplate) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AktorRestConsumer::class.java)
    }

    private val username = EnvironmentUtil.getEnvVar("srv_username", "username")
    private val aktoerregisterV1Url = EnvironmentUtil.getEnvVar("aktoerregister_v1_url", "https://aktoerregisteret.no/api/v1")
    private val aktoerIdIdentGruppe = "AktoerId"

    fun getAktorId(fnr: String) : String {
        return getIdent(fnr, aktoerIdIdentGruppe)
    }

    private fun getIdent(sokeIdent: String, identgruppe: String) : String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", "Bearer " + tokenConsumer.token.access_token)
        headers.set("Nav-Call-Id", getFromMDC(CALL_ID))
        headers.set("Nav-Consumer-Id", username)
        headers.set("Nav-Personidenter", sokeIdent)

        val uriString = UriComponentsBuilder
                .fromHttpUrl("$aktoerregisterV1Url/identer")
                .queryParam("gjeldende", "true")
                .queryParam("identgruppe", identgruppe)
                .toUriString()
        try {
            val result = restTemplate
                    .exchange(uriString, HttpMethod.GET, HttpEntity<Any>(headers), AktorResponse::class.java)

            if (result.statusCode != HttpStatus.OK) {
                val message = "Kall mot aktørregister feiler med HTTP-" + result.statusCode
                LOG.error(message)
                throw RuntimeException(message)
            }

            return result
                    .body
                    ?.get(sokeIdent)
                    .let { aktor ->
                        aktor?.identer ?: throw RuntimeException("Fant ikke aktøren: " + aktor?.feilmelding)
                    }
                    .filter { ident -> ident.gjeldende }
                    .map { ident -> ident.ident }
                    .first()


        } catch (e: HttpClientErrorException) {
            LOG.error("Feil ved oppslag i aktørtjenesten", e)
            throw RuntimeException(e)
        }

    }

}