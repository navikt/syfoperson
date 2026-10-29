package no.nav.syfo.naringskode

data class Naringskode(
    val code: String,
    val name: String,
    val parentCode: String?,
    val shortName: String? = null,
)

data class NaringskodeHierarki(
    val standard: Int,
    val hierarki: List<Naringskode>,
)
