package no.nav.syfo.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.util.Arrays.asList

@Configuration
@EnableCaching
class ApplicationConfig {
    @Bean
    fun restTemplate(vararg interceptors: ClientHttpRequestInterceptor): RestTemplate {
        val template = RestTemplate()
        template.interceptors = asList<ClientHttpRequestInterceptor>(*interceptors)
        return template
    }
}
