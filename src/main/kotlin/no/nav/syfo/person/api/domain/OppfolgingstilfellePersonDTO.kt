package no.nav.syfo.person.api.domain

import java.time.LocalDate

data class OppfolgingstilfellePersonDTO(
    val fom: LocalDate? = null,
    val tom: LocalDate? = null,
)
