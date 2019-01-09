package no.nav.syfo.localconfig

import no.nav.security.spring.oidc.test.TokenGeneratorConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.util.Arrays.asList

@Configuration
@Import(TokenGeneratorConfiguration::class)
class LocalApplicationConfig(environment: Environment)/*
            Her kan du ta inn properties som normalt settes av platformen slik at de er tilgjengelige runtime lokalt
            Eks: System.setProperty("syfoperson_USERNAME", environment.getProperty("syfoperson.username"));
         */
