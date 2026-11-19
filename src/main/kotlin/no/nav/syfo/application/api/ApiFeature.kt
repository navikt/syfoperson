package no.nav.syfo.application.api

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import no.nav.syfo.application.metric.METRICS_REGISTRY
import no.nav.syfo.util.*
import java.time.Duration
import java.util.*
import java.util.concurrent.CancellationException

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
        jackson {
            configure()
        }
    }
}

fun Application.installStatusPages() {
    install(StatusPages) {
        // Client disconnected before the response was sent; rethrow so cancellation propagates
        exception<CancellationException> { call, cause ->
            call.application.log.debug("Request cancelled: ${cause.message}")
            throw cause
        }
        exception<Throwable> { call, cause ->
            call.application.log
                .atError()
                .setCause(cause)
                .setMessage("Caught exception, callId=${call.getCallId()}, consumerId=${call.getConsumerId()}: ${cause.message}")
                .log()

            var isUnexpectedException = false
            val responseStatus =
                when (cause) {
                    is BadRequestException -> HttpStatusCode.BadRequest
                    is NotFoundException -> HttpStatusCode.NotFound
                    is IllegalStateException -> HttpStatusCode.Conflict
                    is IllegalArgumentException -> HttpStatusCode.BadRequest
                    else -> {
                        isUnexpectedException = true
                        HttpStatusCode.InternalServerError
                    }
                }

            val message =
                if (isUnexpectedException) {
                    "The server reported an unexpected error and cannot complete the request."
                } else {
                    cause.message ?: "Unknown error"
                }
            call.respond(responseStatus, message)
        }
    }
}
