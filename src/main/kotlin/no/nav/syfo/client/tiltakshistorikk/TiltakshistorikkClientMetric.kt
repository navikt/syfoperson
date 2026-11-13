package no.nav.syfo.client.tiltakshistorikk

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Counter.builder
import no.nav.syfo.application.metric.METRICS_NS
import no.nav.syfo.application.metric.METRICS_REGISTRY

private const val CALL_TILTAKSHISTORIKK_BASE = "${METRICS_NS}_call_tiltakshistorikk"

val COUNT_CALL_TILTAKSHISTORIKK_SUCCESS: Counter = builder("${CALL_TILTAKSHISTORIKK_BASE}_success_count")
    .description("Counts successful calls to tiltakshistorikk")
    .register(METRICS_REGISTRY)

val COUNT_CALL_TILTAKSHISTORIKK_CACHE_HIT: Counter = builder("${CALL_TILTAKSHISTORIKK_BASE}_cache_hit_count")
    .description("Counts cache hits for tiltakshistorikk")
    .register(METRICS_REGISTRY)

val COUNT_CALL_TILTAKSHISTORIKK_CACHE_MISS: Counter = builder("${CALL_TILTAKSHISTORIKK_BASE}_cache_miss_count")
    .description("Counts cache misses for tiltakshistorikk")
    .register(METRICS_REGISTRY)

val COUNT_CALL_TILTAKSHISTORIKK_FAIL: Counter = builder("${CALL_TILTAKSHISTORIKK_BASE}_fail_count")
    .description("Counts failed calls to tiltakshistorikk")
    .register(METRICS_REGISTRY)

val COUNT_TILTAKSHISTORIKK_SKIPPED_ELEMENT: Counter = builder("${CALL_TILTAKSHISTORIKK_BASE}_skipped_element_count")
    .description("Counts elements from tiltakshistorikk that could not be parsed and were skipped")
    .register(METRICS_REGISTRY)
