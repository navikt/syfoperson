package no.nav.syfo.localconfig

import no.nav.security.spring.oidc.test.TokenGeneratorConfiguration
import org.springframework.context.annotation.*
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@Configuration
@Import(TokenGeneratorConfiguration::class)
class LocalApplicationConfig
