package no.nav.syfo

import no.nav.security.spring.oidc.validation.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableOIDCTokenValidation(ignore=["org.springframework"])
@SpringBootApplication
class LocalApplication
    fun main(args: Array<String>) {
        runApplication<LocalApplication>(*args)
    }
