package no.nav.syfo.client.kodeverk

data class Postinformasjon(
    val postnummer: String,
    val poststed: String?,
)

fun List<Postinformasjon>.getPoststedByPostnummer(postnummer: String?): String? =
    this.firstOrNull { it.postnummer == postnummer }?.poststed
