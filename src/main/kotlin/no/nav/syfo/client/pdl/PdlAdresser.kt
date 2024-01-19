package no.nav.syfo.client.pdl

import java.time.LocalDate
import java.time.LocalDateTime

data class PdlBostedsadresse(
    val angittFlyttedato: LocalDate?,
    val gyldigFraOgMed: LocalDateTime?,
    val gyldigTilOgMed: LocalDateTime?,
    val coAdressenavn: String?,
    val vegadresse: PdlVegadresse?,
    val matrikkeladresse: PdlMatrikkeladresse?,
    val utenlandskAdresse: PdlUtenlandskAdresse?,
    val ukjentBosted: PdlUkjentBosted?
)

data class PdlKontaktadresse(
    val gyldigFraOgMed: LocalDateTime?,
    val gyldigTilOgMed: LocalDateTime?,
    val type: PdlKontaktadresseType,
    val coAdressenavn: String?,
    val postboksadresse: PdlPostboksadresse?,
    val vegadresse: PdlVegadresse?,
    val postadresseIFrittFormat: PdlPostadresseIFrittFormat?,
    val utenlandskAdresse: PdlUtenlandskAdresse?,
    val utenlandskAdresseIFrittFormat: PdlUtenlandskAdresseIFrittFormat?
)

enum class PdlKontaktadresseType {
    Innland, Utland
}

data class PdlOppholdsadresse(
    val gyldigFraOgMed: LocalDateTime?,
    val gyldigTilOgMed: LocalDateTime?,
    val coAdressenavn: String?,
    val utenlandskAdresse: PdlUtenlandskAdresse?,
    val vegadresse: PdlVegadresse?,
    val matrikkeladresse: PdlMatrikkeladresse?,
    val oppholdAnnetSted: String?
)

data class PdlPostadresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postnummer: String?
)

data class PdlPostboksadresse(
    val postbokseier: String?,
    val postboks: String,
    val postnummer: String?
)

data class PdlUtenlandskAdresse(
    val adressenavnNummer: String?,
    val bygningEtasjeLeilighet: String?,
    val postboksNummerNavn: String?,
    val postkode: String?,
    val bySted: String?,
    val regionDistriktOmraade: String?,
    val landkode: String
)

data class PdlUtenlandskAdresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postkode: String?,
    val byEllerStedsnavn: String?,
    val landkode: String
)

data class PdlVegadresse(
    val matrikkelId: Long?,
    val husnummer: String?,
    val husbokstav: String?,
    val bruksenhetsnummer: String?,
    val adressenavn: String?,
    val kommunenummer: String?,
    val bydelsnummer: String?,
    val tilleggsnavn: String?,
    val postnummer: String?
)

data class PdlMatrikkeladresse(
    val matrikkelId: Long?,
    val bruksenhetsnummer: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val kommunenummer: String?
)

data class PdlUkjentBosted(
    val bostedskommune: String?
)
