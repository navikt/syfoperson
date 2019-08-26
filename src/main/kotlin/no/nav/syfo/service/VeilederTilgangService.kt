package no.nav.syfo.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
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
        @Value("\${tilgangskontrollapi.url}") tilgangskontrollUrl: String,
        val template: RestTemplate
) : InitializingBean {
    private var instance: VeilederTilgangService? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    private val tilgangTilBrukerViaAzureUriTemplate: UriComponentsBuilder

    init {
        tilgangTilBrukerViaAzureUriTemplate = fromHttpUrl(tilgangskontrollUrl)
                .path(TILGANG_TIL_BRUKER_VIA_AZURE_PATH)
                .queryParam(FNR, FNR_PLACEHOLDER)
    }

    fun sjekkVeiledersTilgangTilPersonViaAzure(fnr: String): Boolean {
        val tilgangTilBrukerViaAzureUriMedFnr = tilgangTilBrukerViaAzureUriTemplate.build(singletonMap<String, String>(FNR, fnr))
        return kallUriMedTemplate(tilgangTilBrukerViaAzureUriMedFnr)
    }

    private fun kallUriMedTemplate(uri: URI): Boolean {
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
        const val TILGANG_TIL_BRUKER_VIA_AZURE_PATH = "/bruker"
        private const val FNR_PLACEHOLDER = "{$FNR}"
    }

}
