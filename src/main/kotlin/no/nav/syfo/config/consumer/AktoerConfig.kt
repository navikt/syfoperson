package no.nav.syfo.config.consumer

import no.nav.syfo.consumer.util.ws.LogErrorHandler
import no.nav.syfo.consumer.util.ws.STSClientConfig
import no.nav.syfo.consumer.util.ws.WsClient
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.Collections.singletonList

@Configuration
class AktoerConfig {

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = "mockAktoer_V2", havingValue = "false", matchIfMissing = true)
    @Primary
    fun aktoerV2(@Value("\${aktoer.v2.endpointurl}") serviceUrl: String) : AktoerV2 {
        val port: AktoerV2 = WsClient<AktoerV2>().createPort(serviceUrl, AktoerV2::class.java, singletonList(LogErrorHandler()))
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

}