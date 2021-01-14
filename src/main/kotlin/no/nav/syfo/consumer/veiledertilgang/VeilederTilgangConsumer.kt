package no.nav.syfo.consumer.veiledertilgang

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil.tokenFraOIDC
import no.nav.syfo.metric.Metric
import no.nav.syfo.person.api.domain.Fnr
import no.nav.syfo.util.bearerHeader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import java.net.URI
import java.util.Collections.singletonMap
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@Service
class VeilederTilgangConsumer @Inject constructor(
    @Value("\${tilgangskontrollapi.url}") private val tilgangskontrollUrl: String,
    private val metric: Metric,
    private val template: RestTemplate,
    private val contextHolder: TokenValidationContextHolder
) {
    private val tilgangTilBrukerViaAzureUriTemplate: UriComponentsBuilder

    init {
        tilgangTilBrukerViaAzureUriTemplate = fromHttpUrl(tilgangskontrollUrl)
            .path(ACCESS_TO_USER_WITH_AZURE_PATH)
            .queryParam(FNR, FNR_PLACEHOLDER)
    }

    fun throwExceptionIfDeniedAccess(fnr: Fnr) {
        val hasAccess = hasVeilederAccessToPersonWithAzure(fnr.fnr)
        if (!hasAccess) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToPersonWithAzure(fnr: String): Boolean {
        val tilgangTilBrukerViaAzureUriMedFnr = tilgangTilBrukerViaAzureUriTemplate.build(singletonMap(FNR, fnr))
        return callUriWithTemplate(tilgangTilBrukerViaAzureUriMedFnr)
    }

    private fun callUriWithTemplate(uri: URI): Boolean {
        return try {
            val response = template.exchange(
                uri,
                HttpMethod.GET,
                createEntity(),
                String::class.java
            )
            return response.statusCode.is2xxSuccessful
        } catch (e: HttpClientErrorException) {
            if (e.rawStatusCode == 403) {
                false
            } else {
                LOG.error("HttpClientErrorException mot tilgangskontroll", e)
                metric.countEvent("call_tilgangskontroll_denied")
                return false
            }
        } catch (e: HttpServerErrorException) {
            LOG.error("HttpServerErrorException mot tilgangskontroll med status ${e.rawStatusCode}", e)
            metric.countEvent("call_tilgangskontroll_fail")
            return false
        }
    }

    private fun createEntity(): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.set(HttpHeaders.AUTHORIZATION, bearerHeader(tokenFraOIDC(contextHolder, OIDCIssuer.AZURE)))
        return HttpEntity(headers)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(VeilederTilgangConsumer::class.java)
        const val FNR = "fnr"
        const val ACCESS_TO_USER_WITH_AZURE_PATH = "/bruker"
        private const val FNR_PLACEHOLDER = "{$FNR}"
    }
}
