package no.nav.syfo.client.aareg

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Counter.builder
import no.nav.syfo.application.metric.METRICS_NS
import no.nav.syfo.application.metric.METRICS_REGISTRY

const val CALL_AAREG_BASE = "${METRICS_NS}_call_aareg"

const val CALL_AAREG_ARBEIDSFORHOLD_BASE = "${CALL_AAREG_BASE}_arbeidsforhold"
const val CALL_AAREG_ARBEIDSFORHOLD_SUCCESS = "${CALL_AAREG_ARBEIDSFORHOLD_BASE}_success_count"
const val CALL_AAREG_ARBEIDSFORHOLD_CACHE_HIT = "${CALL_AAREG_ARBEIDSFORHOLD_BASE}_cache_hit_count"
const val CALL_AAREG_ARBEIDSFORHOLD_CACHE_MISS = "${CALL_AAREG_ARBEIDSFORHOLD_BASE}_cache_miss_count"
const val CALL_AAREG_ARBEIDSFORHOLD_FAIL = "${CALL_AAREG_ARBEIDSFORHOLD_BASE}_fail_count"

val COUNT_CALL_AAREG_ARBEIDSFORHOLD_SUCCESS: Counter = builder(CALL_AAREG_ARBEIDSFORHOLD_SUCCESS)
    .description("Counts the number of successful calls to Aareg - Arbeidsforhold")
    .register(METRICS_REGISTRY)

val COUNT_CALL_AAREG_ARBEIDSFORHOLD_CACHE_HIT: Counter = builder(CALL_AAREG_ARBEIDSFORHOLD_CACHE_HIT)
    .description("Counts the number of cache hits - Arbeidsforhold")
    .register(METRICS_REGISTRY)

val COUNT_CALL_AAREG_ARBEIDSFORHOLD_CACHE_MISS: Counter = builder(CALL_AAREG_ARBEIDSFORHOLD_CACHE_MISS)
    .description("Counts the number of cache miss - Arbeidsforhold")
    .register(METRICS_REGISTRY)

val COUNT_CALL_AAREG_ARBEIDSFORHOLD_FAIL: Counter = builder(CALL_AAREG_ARBEIDSFORHOLD_FAIL)
    .description("Counts the number of failed calls to Aareg - Arbeidsforhold")
    .register(METRICS_REGISTRY)
