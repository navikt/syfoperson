package no.nav.syfo.person.api.domain

data class PersonInfo(
    val fnr: String,
    val navn: String,
    val skjermingskode: Skjermingskode
)
