package no.nav.syfo.pdl

import no.nav.syfo.util.lowerCapitalize
import java.io.Serializable

data class PdlPersonResponse(
        val errors: List<PdlError>?,
        val data: PdlHentPerson?
)

data class PdlError(
        val message: String,
        val locations: List<PdlErrorLocation>,
        val path: List<String>?,
        val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
        val line: Int?,
        val column: Int?
)

data class PdlErrorExtension(
        val code: String?,
        val classification: String
)

data class PdlHentPerson(
        val hentPerson: PdlPerson?
) : Serializable

data class PdlPerson(
        val navn: List<PdlPersonNavn>,
        val adressebeskyttelse: List<Adressebeskyttelse>?
) : Serializable

data class PdlPersonNavn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
) : Serializable

data class Adressebeskyttelse(
        val gradering: Gradering
) : Serializable

enum class Gradering : Serializable {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}


fun PdlHentPerson.getDiskresjonskode(): String {
    val adressebeskyttelseList = this.hentPerson?.adressebeskyttelse
    if (adressebeskyttelseList.isNullOrEmpty()) {
        return ""
    } else {
        val adressebeskyttelse = adressebeskyttelseList.first()
        return when {
            adressebeskyttelse.isKode6() -> {
                "6"
            }
            adressebeskyttelse.isKode7() -> {
                "7"
            }
            else -> {
                ""
            }
        }
    }
}

fun PdlHentPerson.isKode6Or7(): Boolean {
    val adressebeskyttelse = this.hentPerson?.adressebeskyttelse
    return if (adressebeskyttelse.isNullOrEmpty()) {
        false
    } else {
        return adressebeskyttelse.any {
            it.isKode6() || it.isKode7()
        }
    }
}

fun Adressebeskyttelse.isKode6(): Boolean {
    return this.gradering == Gradering.STRENGT_FORTROLIG || this.gradering == Gradering.STRENGT_FORTROLIG_UTLAND
}

fun Adressebeskyttelse.isKode7(): Boolean {
    return this.gradering == Gradering.FORTROLIG
}

fun PdlHentPerson.getName(): String? {
    val nameList = this.hentPerson?.navn
    if (nameList.isNullOrEmpty()) {
        return null
    }
    nameList[0].let {
        val firstName = it.fornavn.lowerCapitalize()
        val middleName = it.mellomnavn
        val surName = it.etternavn.lowerCapitalize()

        return if (middleName.isNullOrBlank()) {
            "$firstName $surName"
        } else {
            "$firstName ${middleName.lowerCapitalize()} $surName"
        }
    }
}
