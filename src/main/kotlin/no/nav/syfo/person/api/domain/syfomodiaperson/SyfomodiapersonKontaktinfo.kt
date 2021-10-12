package no.nav.syfo.person.api.domain.syfomodiaperson

data class SyfomodiapersonKontaktinfo(
    val fnr: String,
    val epost: String? = null,
    val tlf: String? = null,
    val skalHaVarsel: Boolean,
)
