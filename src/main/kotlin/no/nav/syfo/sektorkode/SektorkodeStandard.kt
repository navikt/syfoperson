package no.nav.syfo.sektorkode

enum class SektorkodeStandard(val year: Int, val ssbVersion: Int, val levels: Int) {
    IS1987(1987, 93, 3),
    IS2012(2012, 92, 3),
}
