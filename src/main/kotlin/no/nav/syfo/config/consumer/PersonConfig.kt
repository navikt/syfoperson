package no.nav.syfo.config.consumer


import no.nav.syfo.consumer.util.ws.LogErrorHandler
import no.nav.syfo.consumer.util.ws.STSClientConfig
import no.nav.syfo.consumer.util.ws.WsClient
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.Collections.singletonList

@Configuration
class PersonConfig {

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = "mockPerson_V3", havingValue = "false", matchIfMissing = true)
    @Primary
    fun personV3(@Value("\${virksomhet.person.v3.endpointurl}") serviceUrl: String) : PersonV3 {
        val port: PersonV3 = WsClient<PersonV3>().createPort(serviceUrl, PersonV3::class.java, singletonList(LogErrorHandler()))
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

}