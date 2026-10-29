package no.nav.syfo.naringskode

enum class NaringskodeStandard(val year: Int, val ssbVersion: Int, val levels: Int) {
    SN1994(1994, 32, 6),
    SN2002(2002, 31, 6),
    SN2007(2007, 30, 5),
    SN2025(2025, 3218, 5),
}
