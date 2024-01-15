package no.nav.syfo.client.kodeverk

data class Postinformasjon(
    val postnummer: String,
    val poststed: String?, // TODO: Skal vi godta at denne er null?
)
