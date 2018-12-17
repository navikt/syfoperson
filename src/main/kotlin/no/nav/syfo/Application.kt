package no.nav.syfo

import no.nav.security.spring.oidc.validation.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableOIDCTokenValidation(ignore = ["org.springframework"])
@SpringBootApplication
@EnableCaching
class Application

    fun main(args: Array<String>) {
        runApplication<Application>(*args)
    }
