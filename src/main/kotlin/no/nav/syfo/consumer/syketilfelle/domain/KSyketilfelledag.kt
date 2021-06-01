package no.nav.syfo.consumer.syketilfelle.domain

import java.time.LocalDate

data class KSyketilfelledag(
    val dag: LocalDate,
    val prioritertSyketilfellebit: KSyketilfellebit?,
)
