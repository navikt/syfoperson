package no.nav.syfo.application.api

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import no.nav.syfo.application.metric.METRICS_REGISTRY
import no.nav.syfo.util.*
import java.time.Duration
import java.util.*

fun Application.installMetrics() {
    install(MicrometerMetrics) {
        registry = METRICS_REGISTRY
        distributionStatisticConfig = DistributionStatisticConfig.Builder()
            .percentilesHistogram(true)
            .maximumExpectedValue(Duration.ofSeconds(20).toNanos().toDouble())
            .build()
    }
}

fun Application.installCallId() {
    install(CallId) {
        header(NAV_CALL_ID_HEADER)
        generate { "generated-${UUID.randomUUID()}" }
        verify { callId: String -> callId.isNotEmpty() }
    }
}

fun Application.installContentNegotiation() {
    install(ContentNegotiation) {
        jackson(
            block = {
                this.applyConfig()
            }
        )
    }
}

fun Application.installStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")
            val callId = call.getCallId()
            val consumerId = call.getConsumerId()
            call.application.log.error("Caught exception, callId=$callId, consumerId=$consumerId", cause)
            throw cause
        }
    }
}
