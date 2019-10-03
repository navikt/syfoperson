package no.nav.syfo.service

import no.nav.syfo.config.EnvironmentUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import java.net.URI
import java.util.Collections.singletonMap
import javax.inject.Inject

@Service
class VeilederTilgangService @Inject constructor(
        val template: RestTemplate
) : InitializingBean {
    private var instance: VeilederTilgangService? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    private val tilgangskontrollUrl = EnvironmentUtil.getEnvVar("TILGANGSKONTROLLAPI_URL", "http://eksempel.no/tilgangskontroll")

    private val tilgangTilBrukerViaAzureUriTemplate: UriComponentsBuilder

    init {
        tilgangTilBrukerViaAzureUriTemplate = fromHttpUrl(tilgangskontrollUrl)
                .path(ACCESS_TO_USER_WITH_AZURE_PATH)
                .queryParam(FNR, FNR_PLACEHOLDER)
    }

    fun hasVeilederAccessToPersonWithAzure(fnr: String): Boolean {
        val tilgangTilBrukerViaAzureUriMedFnr = tilgangTilBrukerViaAzureUriTemplate.build(singletonMap<String, String>(FNR, fnr))
        return callUriWithTemplate(tilgangTilBrukerViaAzureUriMedFnr)
    }

    private fun callUriWithTemplate(uri: URI): Boolean {
        return try {
            template.getForObject(uri, Any::class.java)
            true
        } catch (e: HttpClientErrorException) {
            if (e.rawStatusCode == 403) {
                false
            } else {
                LOG.error("HttpClientErrorException mot uri {}", uri, e)
                return false
            }
        }
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(VeilederTilgangService::class.java)
        const val FNR = "fnr"
        const val ACCESS_TO_USER_WITH_AZURE_PATH = "/bruker"
        private const val FNR_PLACEHOLDER = "{$FNR}"
    }

}
