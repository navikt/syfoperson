package no.nav.syfo.config.consumer

import no.nav.syfo.config.EnvironmentUtil.getEnvVar
import no.nav.syfo.ws.util.*
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.*

@Configuration
class AktoerConfig {

    private val serviceUrl = getEnvVar("AKTOER_V2", "http://eksempel.no/ws/AktoerV2")

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = [MOCK_KEY], havingValue = "false", matchIfMissing = true)
    @Primary
    fun aktoerV2(): AktoerV2 {
        val port = WsClient<AktoerV2>().createPort(serviceUrl, AktoerV2::class.java, listOf(LogErrorHandler()))
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    companion object {
        const val MOCK_KEY = "aktoer.withmock"
    }
}
