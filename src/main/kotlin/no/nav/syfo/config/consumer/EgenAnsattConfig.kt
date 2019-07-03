package no.nav.syfo.config.consumer

import no.nav.syfo.consumer.util.ws.*
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.*

@Configuration
class EgenAnsattConfig {

    @Bean
    @ConditionalOnProperty(value = ["mockEgenAnsatt_V1"], havingValue = "false", matchIfMissing = true)
    @Primary
    fun egenAnsattV1(@Value("\${virksomhet.egenansatt.v1.endpointurl}") serviceUrl: String): EgenAnsattV1 {
        val port = WsClient<EgenAnsattV1>().createPort(serviceUrl, EgenAnsattV1::class.java, listOf(LogErrorHandler()))
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }
}
