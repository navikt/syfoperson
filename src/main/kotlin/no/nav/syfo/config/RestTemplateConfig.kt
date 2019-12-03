package no.nav.syfo.config

import no.nav.syfo.util.EnvironmentUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun basicAuthRestTemplate() : RestTemplate {
        val username = EnvironmentUtil.getEnvVar("srv_username", "username")
        val password = EnvironmentUtil.getEnvVar("srv_password", "password")

        return RestTemplateBuilder()
                .basicAuthorization(username, password)
                .build()
    }

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun interceptorRestTemplate(vararg interceptors: ClientHttpRequestInterceptor): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.interceptors = listOf(*interceptors)
        return restTemplate
    }
}
