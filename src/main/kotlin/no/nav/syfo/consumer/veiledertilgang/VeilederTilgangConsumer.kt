package no.nav.syfo.consumer.veiledertilgang

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil.tokenFraOIDC
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.person.api.domain.Fnr
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.*
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@Service
class VeilederTilgangConsumer @Inject constructor(
    @Value("\${tilgangskontrollapi.url}") private val tilgangskontrollUrl: String,
    @Value("\${syfotilgangskontroll.client.id}") private val syfotilgangskontrollClientId: String,
    private val metric: Metric,
    private val template: RestTemplate,
    private val contextHolder: TokenValidationContextHolder,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer
) {
    private val tilgangskontrollPersonUrl: String
    private val tilgangskontrollPersonListUrl: String

    init {
        tilgangskontrollPersonUrl = "$tilgangskontrollUrl$TILGANGSKONTROLL_PERSON_PATH"
        tilgangskontrollPersonListUrl = "$tilgangskontrollUrl$TILGANGSKONTROLL_PERSON_LIST_PATH"
    }

    fun throwExceptionIfDeniedAccessAzureOBO(fnr: Fnr) {
        val hasAccess = hasVeilederAccessToPersonWithAzureOBO(fnr)
        if (!hasAccess) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToPersonWithAzureOBO(fnr: Fnr): Boolean {
        val token = tokenFraOIDC(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
        val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
            scopeClientId = syfotilgangskontrollClientId,
            token = token
        )
        try {
            val response = template.exchange(
                tilgangskontrollPersonUrl,
                HttpMethod.GET,
                createEntity(
                    personIdentNumber = fnr,
                    token = oboToken,
                ),
                String::class.java
            )
            return response.statusCode.is2xxSuccessful
        } catch (e: HttpClientErrorException) {
            return if (e.rawStatusCode == 403) {
                false
            } else {
                LOG.error("HttpClientErrorException mot tilgangskontroll", e)
                metric.countEvent("call_tilgangskontroll_denied")
                false
            }
        } catch (e: HttpServerErrorException) {
            LOG.error("HttpServerErrorException mot tilgangskontroll med status ${e.rawStatusCode}", e)
            metric.countEvent("call_tilgangskontroll_fail")
            return false
        }
    }

    fun hasVeilederAccessToPersonListWithOBOO(
        personIdentNumberList: List<Fnr>,
    ): List<String> {
        val token = tokenFraOIDC(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
        val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
            scopeClientId = syfotilgangskontrollClientId,
            token = token
        )
        return try {
            val response = template.exchange(
                tilgangskontrollPersonListUrl,
                HttpMethod.POST,
                createPostEntity(oboToken, personIdentNumberList),
                object : ParameterizedTypeReference<List<String>>() {},
            )
            response.body!!
        } catch (e: RestClientResponseException) {
            LOG.error("RestClientResponseException mot syfo-tilgangskontroll med status ${e.rawStatusCode}", e)
            metric.countEvent("call_tilgangskontroll_fail")
            emptyList()
        }
    }

    private fun createEntity(
        personIdentNumber: Fnr,
        token: String,
    ): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.setBearerAuth(token)
        headers[NAV_PERSONIDENT_HEADER] = personIdentNumber.fnr
        headers[NAV_CALL_ID_HEADER] = createCallId()
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        return HttpEntity(headers)
    }

    private fun createPostEntity(
        token: String,
        personIdentNumberList: List<Fnr>,
    ): HttpEntity<List<String>> {
        val body = personIdentNumberList.map { it.fnr }
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(token)
        return HttpEntity(body, headers)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(VeilederTilgangConsumer::class.java)
        const val TILGANGSKONTROLL_PERSON_LIST_PATH = "/navident/brukere"
        const val TILGANGSKONTROLL_PERSON_PATH = "/navident/person"
    }
}
