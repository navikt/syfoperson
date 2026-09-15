package no.nav.syfo.person.api.domain.syfomodiaperson

enum class Vergemaltype(private val pdlValue: String) {
    ENSLIG_MINDREARIG_ASYLSOEKER("ensligMindreaarigAsylsoeker"),
    ENSLIG_MINDREARIG_FLYKTNING("ensligMindreaarigFlyktning"),
    VOKSEN("voksen"),
    MIDLERTIDIG_FOR_VOKSEN("midlertidigForVoksen"),
    MINDREARIG("mindreaarig"),
    MIDLERTIDIG_FOR_MINDREARIG("midlertidigForMindreaarig"),
    FORVALTNING_UTENFOR_VERGEMAL("forvaltningUtenforVergemaal"),
    STADFESTET_FREMTIDSFULLMAKT("stadfestetFremtidsfullmakt"),
    UKJENT("ukjent");

    companion object {
        fun fromPdlValue(pdlValue: String?): Vergemaltype =
            entries.firstOrNull { it.pdlValue == pdlValue } ?: UKJENT
    }
}
