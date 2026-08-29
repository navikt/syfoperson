package no.nav.syfo.client.aap

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Counter.builder
import no.nav.syfo.application.metric.METRICS_NS
import no.nav.syfo.application.metric.METRICS_REGISTRY

private const val CALL_AAP_SAKER_BASE = "${METRICS_NS}_call_aap_saker"

val COUNT_CALL_AAP_SAKER_SUCCESS: Counter = builder("${CALL_AAP_SAKER_BASE}_success_count")
    .description("Counts successful calls to aap-api-intern")
    .register(METRICS_REGISTRY)

val COUNT_CALL_AAP_SAKER_CACHE_HIT: Counter = builder("${CALL_AAP_SAKER_BASE}_cache_hit_count")
    .description("Counts cache hits for AAP cases")
    .register(METRICS_REGISTRY)

val COUNT_CALL_AAP_SAKER_CACHE_MISS: Counter = builder("${CALL_AAP_SAKER_BASE}_cache_miss_count")
    .description("Counts cache misses for AAP cases")
    .register(METRICS_REGISTRY)

val COUNT_CALL_AAP_SAKER_FAIL: Counter = builder("${CALL_AAP_SAKER_BASE}_fail_count")
    .description("Counts failed calls to aap-api-intern")
    .register(METRICS_REGISTRY)
