package no.nav.syfo.config

import no.nav.security.spring.oidc.validation.api.EnableOIDCTokenValidation
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class ApplicationConfig
