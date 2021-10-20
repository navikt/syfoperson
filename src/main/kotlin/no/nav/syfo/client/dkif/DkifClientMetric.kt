package no.nav.syfo.client.dkif

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Counter.builder
import no.nav.syfo.application.metric.METRICS_NS
import no.nav.syfo.application.metric.METRICS_REGISTRY

const val CALL_DKIF_BASE = "${METRICS_NS}_call_dkif"

const val CALL_DKIF_KONTAKTINFORMASJON_BASE = "${CALL_DKIF_BASE}_kontaktinformasjon"
const val CALL_DKIF_KONTAKTINFORMASJON_SUCCESS = "${CALL_DKIF_KONTAKTINFORMASJON_BASE}_success_count"
const val CALL_DKIF_KONTAKTINFORMASJON_FAIL = "${CALL_DKIF_KONTAKTINFORMASJON_BASE}_fail_count"

val COUNT_CALL_DKIF_KONTAKTINFORMASJON_SUCCESS: Counter = builder(CALL_DKIF_KONTAKTINFORMASJON_SUCCESS)
    .description("Counts the number of successful calls to DKIF - Kontaktinformasjon")
    .register(METRICS_REGISTRY)

val COUNT_CALL_DKIF_KONTAKTINFORMASJON_FAIL: Counter = builder(CALL_DKIF_KONTAKTINFORMASJON_FAIL)
    .description("Counts the number of failed calls to DKIF - Kontaktinformasjon")
    .register(METRICS_REGISTRY)
