package no.nav.syfo.person.api.domain

data class Fnr(val fnr: String = "") {
    private val elevenDigits = Regex("^\\d{11}\$")

    init {
        if (!elevenDigits.matches(fnr)) {
            throw IllegalArgumentException("Value is not a valid PersonIdentNumber")
        }
    }
}
