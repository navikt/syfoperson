package no.nav.syfo.application.metric.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.application.metric.METRICS_REGISTRY

fun Routing.registerMetricApi() {
    get("/internal/metrics") {
        call.respondText(METRICS_REGISTRY.scrape())
    }
}
