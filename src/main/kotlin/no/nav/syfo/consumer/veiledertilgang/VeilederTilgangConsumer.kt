package no.nav.syfo.consumer.veiledertilgang

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil.tokenFraOIDC
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.person.api.domain.Fnr
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.*
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import java.net.URI
import java.util.Collections.singletonMap
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
        val token = tokenFraOIDC(contextHolder, OIDCIssuer.AZURE)
        return try {
            val response = template.exchange(
                uri,
                HttpMethod.GET,
                createEntity(token),
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

    fun throwExceptionIfDeniedAccessAzureOBO(fnr: Fnr) {
        val hasAccess = hasVeilederAccessToPersonWithAzureOBO(fnr)
        if (!hasAccess) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToPersonWithAzureOBO(fnr: Fnr): Boolean {
        try {
            val token = tokenFraOIDC(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
            val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
                scopeClientId = syfotilgangskontrollClientId,
                token = token
            )

            val response = template.exchange(
                accessToUserV2Url(fnr),
                HttpMethod.GET,
                createEntity(oboToken),
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

    private fun createEntity(token: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.setBearerAuth(token)
        return HttpEntity(headers)
    }

    fun accessToUserV2Url(fnr: Fnr): String {
        return "$tilgangskontrollUrl$ACCESS_TO_USER_WITH_AZURE_V2_PATH/${fnr.fnr}"
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(VeilederTilgangConsumer::class.java)
        const val FNR = "fnr"
        const val ACCESS_TO_USER_WITH_AZURE_V2_PATH = "/navident/bruker"
        const val ACCESS_TO_USER_WITH_AZURE_PATH = "/bruker"
        private const val FNR_PLACEHOLDER = "{$FNR}"
    }
}
