package no.nav.syfo

import no.nav.security.spring.oidc.validation.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableOIDCTokenValidation(ignore=["org.springframework"])
@SpringBootApplication
@EnableCaching
class LocalApplication
    fun main(args: Array<String>) {
        runApplication<LocalApplication>(*args)
    }
