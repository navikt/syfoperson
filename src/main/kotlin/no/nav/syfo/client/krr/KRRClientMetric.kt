package no.nav.syfo.client.krr

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Counter.builder
import no.nav.syfo.application.metric.METRICS_NS
import no.nav.syfo.application.metric.METRICS_REGISTRY

const val CALL_KRR_BASE = "${METRICS_NS}_call_krr"

const val CALL_KRR_KONTAKTINFORMASJON_BASE = "${CALL_KRR_BASE}_kontaktinformasjon"
const val CALL_KRR_KONTAKTINFORMASJON_SUCCESS = "${CALL_KRR_KONTAKTINFORMASJON_BASE}_success_count"
const val CALL_KRR_KONTAKTINFORMASJON_CACHE_HIT = "${CALL_KRR_KONTAKTINFORMASJON_BASE}_cache_hit_count"
const val CALL_KRR_KONTAKTINFORMASJON_CACHE_MISS = "${CALL_KRR_KONTAKTINFORMASJON_BASE}_cache_miss_count"
const val CALL_KRR_KONTAKTINFORMASJON_FAIL = "${CALL_KRR_KONTAKTINFORMASJON_BASE}_fail_count"

val COUNT_CALL_KRR_KONTAKTINFORMASJON_SUCCESS: Counter = builder(CALL_KRR_KONTAKTINFORMASJON_SUCCESS)
    .description("Counts the number of successful calls to KRR - Kontaktinformasjon")
    .register(METRICS_REGISTRY)

val COUNT_CALL_KRR_KONTAKTINFORMASJON_CACHE_HIT: Counter = builder(CALL_KRR_KONTAKTINFORMASJON_CACHE_HIT)
    .description("Counts the number of cache hits - Kontaktinformasjon")
    .register(METRICS_REGISTRY)

val COUNT_CALL_KRR_KONTAKTINFORMASJON_CACHE_MISS: Counter = builder(CALL_KRR_KONTAKTINFORMASJON_CACHE_MISS)
    .description("Counts the number of cache miss - Kontaktinformasjon")
    .register(METRICS_REGISTRY)

val COUNT_CALL_KRR_KONTAKTINFORMASJON_FAIL: Counter = builder(CALL_KRR_KONTAKTINFORMASJON_FAIL)
    .description("Counts the number of failed calls to KRR - Kontaktinformasjon")
    .register(METRICS_REGISTRY)
