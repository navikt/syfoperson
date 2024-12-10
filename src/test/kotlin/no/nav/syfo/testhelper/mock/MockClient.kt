package no.nav.syfo.testhelper.mock

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import no.nav.syfo.application.Environment
import no.nav.syfo.client.commonConfig

fun getMockHttpClient(env: Environment) = HttpClient(MockEngine) {
    commonConfig()
    engine {
        addHandler { request ->
            val requestUrl = request.url.encodedPath
            when {
                requestUrl == "/${env.azureOpenidConfigTokenEndpoint}" -> azureAdMockResponse()
                requestUrl.startsWith("/${env.istilgangskontrollUrl}") -> tilgangskontrollResponse(
                    request
                )
                requestUrl.startsWith("/${env.skjermedePersonerPipUrl}") -> getSkjermedePersonerResponse(request)
                requestUrl.startsWith("/${env.pdlUrl}") -> pdlMockResponse(request)
                requestUrl.startsWith("/${env.krrUrl}") -> krrMockResponse(request)
                requestUrl.startsWith("/${env.kodeverkUrl}") -> kodeverkMockResponse(request)

                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
    }
}
