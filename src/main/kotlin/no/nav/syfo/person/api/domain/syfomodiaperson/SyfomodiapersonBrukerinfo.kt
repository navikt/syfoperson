package no.nav.syfo.person.api.domain.syfomodiaperson

import java.time.LocalDate

data class SyfomodiapersonBrukerinfo(
    val navn: String? = null,
    val kontaktinfo: SyfomodiapersonKontaktinfo,
    val arbeidssituasjon: String = "ARBEIDSTAKER",
    val doedsdato: LocalDate? = null,
)
