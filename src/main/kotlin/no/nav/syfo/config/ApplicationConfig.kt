package no.nav.syfo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.retry.annotation.EnableRetry
import org.springframework.web.client.RestTemplate

@Configuration
@EnableRetry
class ApplicationConfig {
    @Bean
    fun restTemplate(vararg interceptors: ClientHttpRequestInterceptor): RestTemplate {
        val template = RestTemplate()
        template.interceptors = listOf(*interceptors)
        return template
    }
}
