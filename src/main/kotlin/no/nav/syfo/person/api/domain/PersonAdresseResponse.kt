package no.nav.syfo.person.api.domain

import no.nav.syfo.client.pdl.*

data class PersonAdresseResponse(
    val navn: String,
    val bostedsadresse: Bostedsadresse?,
    val kontaktadresse: Kontaktadresse?,
    val oppholdsadresse: Oppholdsadresse?,
)
