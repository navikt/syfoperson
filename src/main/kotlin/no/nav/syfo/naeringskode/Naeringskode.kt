package no.nav.syfo.naeringskode

enum class NaeringskodeStandard(val year: Int, val ssbVersion: Int) {
    SN2007(2007, 30),
    SN2025(2025, 3218),
}

data class Naeringskode(
    val code: String,
    val name: String,
    val parentCode: String?,
    val shortName: String? = null,
)

data class NaeringskodeHierarki(
    val standard: Int,
    val hierarki: List<Naeringskode>,
)
