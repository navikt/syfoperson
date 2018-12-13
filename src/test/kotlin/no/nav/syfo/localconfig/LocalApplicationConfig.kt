package no.nav.syfo.localconfig

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.util.Arrays.asList

@Configuration
class LocalApplicationConfig(environment: Environment)/*
            Her kan du ta inn properties som normalt settes av platformen slik at de er tilgjengelige runtime lokalt
            Eks: System.setProperty("syfoperson_USERNAME", environment.getProperty("syfoperson.username"));
         */

    @Bean
    fun restTemplate(vararg interceptors: ClientHttpRequestInterceptor) : RestTemplate {
        val template = RestTemplate()
        template.interceptors = asList(*interceptors)
        return template
    }
