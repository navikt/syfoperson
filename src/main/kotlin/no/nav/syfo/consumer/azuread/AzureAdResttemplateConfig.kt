package no.nav.syfo.consumer.azuread

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class AzureAdResttemplateConfig {
    @Bean(name = ["restTemplateWithProxy"])
    fun restTemplateMedProxy(): RestTemplate {
        return RestTemplateBuilder()
            .additionalCustomizers(NaisProxyCustomizer())
            .build()
    }
}
