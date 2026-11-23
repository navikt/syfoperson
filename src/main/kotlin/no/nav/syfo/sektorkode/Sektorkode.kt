package no.nav.syfo.sektorkode

data class Sektorkode(
    val code: String,
    val name: String,
    val parentCode: String?,
    val level: Int,
)

data class SektorkodeHierarki(
    val standard: Int,
    val hierarki: List<Sektorkode>,
)
