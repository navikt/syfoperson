package no.nav.syfo.controller.domain

data class PersonInfo(
        val fnr: String = "",
        val navn: String,
        val skjermingskode: Skjermingskode
)