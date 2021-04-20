package no.nav.syfo.config

import org.springframework.context.annotation.*
import org.springframework.web.client.RestTemplate

@Configuration
class ApplicationConfig {
    @Primary
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
