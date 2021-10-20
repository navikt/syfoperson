package no.nav.syfo.person.api.domain

data class PersonInfoRequest(
    val fnr: String,
) {
    private val elevenDigits = Regex("^\\d{11}\$")

    init {
        if (!elevenDigits.matches(fnr)) {
            throw IllegalArgumentException("PersonInfoPersonIdent is not a valid PersonIdentNumber")
        }
    }
}
