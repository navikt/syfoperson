package no.nav.syfo.testhelper

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import no.nav.syfo.util.configure

fun ApplicationTestBuilder.setupApiAndClient(externalMockEnvironment: ExternalMockEnvironment): HttpClient {
    application {
        testApiModule(
            externalMockEnvironment = externalMockEnvironment,
        )
    }
    val client = createClient {
        install(ContentNegotiation) {
            jackson { configure() }
        }
    }

    return client
}
