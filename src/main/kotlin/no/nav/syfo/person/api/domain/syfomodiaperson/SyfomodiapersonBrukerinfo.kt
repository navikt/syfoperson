package no.nav.syfo.person.api.domain.syfomodiaperson

data class SyfomodiapersonBrukerinfo(
    val navn: String? = null,
    val kontaktinfo: SyfomodiapersonKontaktinfo,
    val arbeidssituasjon: String = "ARBEIDSTAKER",
)
