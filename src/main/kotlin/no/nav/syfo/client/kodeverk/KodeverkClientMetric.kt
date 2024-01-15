package no.nav.syfo.client.kodeverk

import io.micrometer.core.instrument.Counter
import no.nav.syfo.application.metric.METRICS_NS
import no.nav.syfo.application.metric.METRICS_REGISTRY

const val CALL_KODEVERK_POSTNUMMER_BASE = "${METRICS_NS}_call_kodeverk_postnummer"
const val CALL_KODEVERK_POSTNUMMER_SUCCESS = "${CALL_KODEVERK_POSTNUMMER_BASE}_success_count"
const val CALL_KODEVERK_POSTNUMMER_FAIL = "${CALL_KODEVERK_POSTNUMMER_BASE}_fail_count"

val COUNT_CALL_KODEVERK_POSTNUMMER_SUCCESS: Counter = Counter.builder(CALL_KODEVERK_POSTNUMMER_SUCCESS)
    .description("Counts the number of successful calls to kodeverk - postnummer")
    .register(METRICS_REGISTRY)
val COUNT_CALL_KODEVERK_POSTNUMMER_FAIL: Counter = Counter.builder(CALL_KODEVERK_POSTNUMMER_FAIL)
    .description("Counts the number of failed calls to kodeverk - postnummer")
    .register(METRICS_REGISTRY)
