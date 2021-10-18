package no.nav.syfo.client.syketilfelle

import io.micrometer.core.instrument.Counter
import no.nav.syfo.application.metric.METRICS_NS
import no.nav.syfo.application.metric.METRICS_REGISTRY

const val CALL_SYKETILFELLE_BASE = "${METRICS_NS}_call_syketilfelle"
const val CALL_SYKETILFELLE_PERSON_BASE = "${CALL_SYKETILFELLE_BASE}_person"

const val CALL_SYKETILFELLE_PERSON_ARBEIDSGIVER_BASE = "${CALL_SYKETILFELLE_PERSON_BASE}_arbeidsgiver"
const val CALL_SYKETILFELLE_PERSON_ARBEIDSGIVER_SUCCESS = "${CALL_SYKETILFELLE_PERSON_ARBEIDSGIVER_BASE}_success_count"
const val CALL_SYKETILFELLE_PERSON_ARBEIDSGIVER_FAIL = "${CALL_SYKETILFELLE_PERSON_ARBEIDSGIVER_BASE}_fail_count"

val COUNT_CALL_SYKETILFELLE_PERSON_ARBEIDSGIVER_SUCCESS: Counter = Counter.builder(CALL_SYKETILFELLE_PERSON_ARBEIDSGIVER_SUCCESS)
    .description("Counts the number of successful calls to syfo-tilgangskontroll - person with arbeidsgiver")
    .register(METRICS_REGISTRY)
val COUNT_CALL_SYKETILFELLE_PERSON_ARBEIDSGIVER_FAIL: Counter = Counter.builder(CALL_SYKETILFELLE_PERSON_ARBEIDSGIVER_FAIL)
    .description("Counts the number of failed calls to Syfosyktilfelle - person with arbeidsgiver")
    .register(METRICS_REGISTRY)

const val CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_BASE = "${CALL_SYKETILFELLE_PERSON_BASE}_no_arbeidsgiver"
const val CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_SUCCESS = "${CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_BASE}_success_count"
const val CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_FAIL = "${CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_BASE}_fail_count"

val COUNT_CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_SUCCESS: Counter = Counter.builder(CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_SUCCESS)
    .description("Counts the number of successful calls to syfo-tilgangskontroll - person without arbeidsgiver")
    .register(METRICS_REGISTRY)
val COUNT_CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_FAIL: Counter = Counter.builder(CALL_SYKETILFELLE_PERSON_NO_ARBEIDSGIVER_FAIL)
    .description("Counts the number of failed calls to Syfosyktilfelle - person without arbeidsgiver")
    .register(METRICS_REGISTRY)
