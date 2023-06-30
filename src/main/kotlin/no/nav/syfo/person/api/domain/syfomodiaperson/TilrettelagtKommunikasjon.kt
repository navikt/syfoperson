package no.nav.syfo.person.api.domain.syfomodiaperson

data class TilrettelagtKommunikasjon(
    val talesprakTolk: Sprak,
    val tegnsprakTolk: Sprak
)

data class Sprak(val value: String)
