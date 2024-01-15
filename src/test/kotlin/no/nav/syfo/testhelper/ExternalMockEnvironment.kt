package no.nav.syfo.testhelper

import io.ktor.server.netty.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.testhelper.mock.*

class ExternalMockEnvironment {
    val applicationState: ApplicationState = testAppState()
    val azureAdMock = AzureAdMock()
    val krrMock = KrrMock()
    val pdlMock = PdlMock()
    val skjermedPersonerPipMock = SkjermedePersonerPipMock()
    val veilederTilgangskontrollMock = VeilederTilgangskontrollMock()
    val kodeverkMock = KodeverkMock()

    val externalApplicationMockMap = hashMapOf(
        azureAdMock.name to azureAdMock.server,
        krrMock.name to krrMock.server,
        pdlMock.name to pdlMock.server,
        skjermedPersonerPipMock.name to skjermedPersonerPipMock.server,
        veilederTilgangskontrollMock.name to veilederTilgangskontrollMock.server,
        kodeverkMock.name to kodeverkMock.server,
    )

    val environment = testEnvironment(
        azureOpenIdTokenEndpoint = azureAdMock.url,
        krrUrl = krrMock.url,
        pdlUrl = pdlMock.url,
        skjermedePersonerPipUrl = skjermedPersonerPipMock.url,
        istilgangskontrollUrl = veilederTilgangskontrollMock.url,
        kodeverkUrl = kodeverkMock.url,
    )

    val redisServer = testRedis(
        port = environment.redisPort,
        secret = environment.redisSecret,
    )

    val wellKnownInternalAzureAD = wellKnownInternalAzureAD()
}

fun ExternalMockEnvironment.startExternalMocks() {
    this.externalApplicationMockMap.start()
    this.redisServer.start()
}

fun ExternalMockEnvironment.stopExternalMocks() {
    this.externalApplicationMockMap.stop()
    this.redisServer.stop()
}

fun HashMap<String, NettyApplicationEngine>.start() {
    this.forEach {
        it.value.start()
    }
}

fun HashMap<String, NettyApplicationEngine>.stop(
    gracePeriodMillis: Long = 1L,
    timeoutMillis: Long = 10L,
) {
    this.forEach {
        it.value.stop(gracePeriodMillis, timeoutMillis)
    }
}
