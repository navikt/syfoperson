package no.nav.syfo.person.api.domain

import java.time.LocalDate

data class OppfolgingstilfelleDTO(
    val virksomhetsnummer: String,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null,
)
