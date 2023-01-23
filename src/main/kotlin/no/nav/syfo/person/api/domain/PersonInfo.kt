package no.nav.syfo.person.api.domain

import java.time.LocalDate

data class PersonInfo(
    val fnr: String,
    val navn: String,
    val skjermingskode: Skjermingskode,
    val dodsdato: LocalDate? = null,
)
