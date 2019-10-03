package no.nav.syfo.metric

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Controller
import javax.inject.Inject

@Controller
class Metric @Inject constructor(private val registry: MeterRegistry) {

    fun tellHttpKall(kode: Int) {
        registry.counter(
                addPrefix("httpstatus"),
                Tags.of(
                        "type", "info",
                        "kode", kode.toString()
                )
        ).increment()
    }

    private fun addPrefix(navn: String): String {
        val metricPrefix = "syfoperson_"
        return metricPrefix + navn
    }
}
