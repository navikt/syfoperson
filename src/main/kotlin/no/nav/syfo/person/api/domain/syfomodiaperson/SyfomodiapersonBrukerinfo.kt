package no.nav.syfo.person.api.domain.syfomodiaperson

import java.time.LocalDate

data class SyfomodiapersonBrukerinfo(
    val aktivPersonident: String,
    val navn: String? = null,
    val arbeidssituasjon: String = "ARBEIDSTAKER",
    val kjonn: String?,
    val fodselsdato: LocalDate?,
    val alder: Int?,
    val dodsdato: LocalDate? = null,
    val tilrettelagtKommunikasjon: TilrettelagtKommunikasjon? = null,
    val sikkerhetstiltak: List<Sikkerhetstiltak>,
)
