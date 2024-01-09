package no.nav.syfo.client.pdl

import no.nav.syfo.person.api.domain.syfomodiaperson.Sikkerhetstiltak
import no.nav.syfo.person.api.domain.syfomodiaperson.Sprak
import no.nav.syfo.person.api.domain.syfomodiaperson.TilrettelagtKommunikasjon
import no.nav.syfo.util.lowerCapitalize
import java.io.Serializable
import java.time.LocalDate

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

fun PdlError.errorMessage(): String {
    return "${this.message} with code: ${extensions.code} and classification: ${extensions.classification}"
}

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
    val adressebeskyttelse: List<Adressebeskyttelse>?,
    val bostedsadresse: List<Bostedsadresse>?,
    val kontaktadresse: List<Kontaktadresse>?,
    val oppholdsadresse: List<Oppholdsadresse>?,
    val doedsfall: List<PdlDoedsfall>?,
    val tilrettelagtKommunikasjon: List<PdlTilrettelagtKommunikasjon>,
    val sikkerhetstiltak: List<PdlSikkerhetstiltak>,
) : Serializable {

    val fullName: String? =
        navn.firstOrNull()?.let {
            val firstName = it.fornavn.lowerCapitalize()
            val middleName = it.mellomnavn
            val surName = it.etternavn.lowerCapitalize()

            if (middleName.isNullOrBlank()) {
                "$firstName $surName"
            } else {
                "$firstName ${middleName.lowerCapitalize()} $surName"
            }
        }

    val isKode6Or7: Boolean =
        adressebeskyttelse?.any {
            it.isKode6() || it.isKode7()
        } ?: false

    val diskresjonskode: String =
        adressebeskyttelse?.firstOrNull()?.let {
            when {
                it.isKode6() -> "6"
                it.isKode7() -> "7"
                else -> ""
            }
        } ?: ""

    fun hentBostedsadresse(): Bostedsadresse? =
        bostedsadresse?.filter { it.gyldigFraOgMed != null }?.maxByOrNull { it.gyldigFraOgMed!! }

    fun hentTilrettelagtKommunikasjon(): TilrettelagtKommunikasjon? =
        tilrettelagtKommunikasjon.firstOrNull()?.let {
            TilrettelagtKommunikasjon(
                talesprakTolk = it.talespraaktolk?.spraak?.let { sprak -> Sprak(sprak) },
                tegnsprakTolk = it.tegnspraaktolk?.spraak?.let { sprak -> Sprak(sprak) },
            )
        }

    fun hentSikkerhetstiltak(): List<Sikkerhetstiltak> = sikkerhetstiltak.map {
        Sikkerhetstiltak(
            type = it.tiltakstype,
            beskrivelse = it.beskrivelse,
            gyldigFom = it.gyldigFraOgMed,
            gyldigTom = it.gyldigTilOgMed
        )
    }

    val dodsdato: LocalDate? = doedsfall?.firstOrNull()?.doedsdato

    fun hentKontaktadresse(): Kontaktadresse? =
        kontaktadresse?.filter { it.gyldigFraOgMed != null }?.maxByOrNull { it.gyldigFraOgMed!! }

    fun hentOppholdsadresse(): Oppholdsadresse? =
        oppholdsadresse?.filter { it.gyldigFraOgMed != null }?.maxByOrNull { it.gyldigFraOgMed!! }
}

data class PdlPersonNavn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
) : Serializable

data class PdlDoedsfall(
    val doedsdato: LocalDate?,
) : Serializable

data class PdlTilrettelagtKommunikasjon(
    val talespraaktolk: PdlSprak?,
    val tegnspraaktolk: PdlSprak?,
) : Serializable

data class PdlSprak(val spraak: String?) : Serializable

data class PdlSikkerhetstiltak(
    val tiltakstype: String,
    val beskrivelse: String,
    val gyldigFraOgMed: LocalDate,
    val gyldigTilOgMed: LocalDate,
)

data class Adressebeskyttelse(
    val gradering: Gradering,
) : Serializable {

    fun isKode6(): Boolean =
        gradering == Gradering.STRENGT_FORTROLIG || gradering == Gradering.STRENGT_FORTROLIG_UTLAND

    fun isKode7(): Boolean =
        gradering == Gradering.FORTROLIG
}

enum class Gradering : Serializable {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT,
}
