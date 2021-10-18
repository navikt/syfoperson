package no.nav.syfo.client.syketilfelle

import java.time.LocalDate

data class KSyketilfelledagDTO(
    val dag: LocalDate,
    val prioritertSyketilfellebit: KSyketilfellebit?,
)
