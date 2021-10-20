package no.nav.syfo.client.pdl

import java.time.LocalDate
import java.time.LocalDateTime

data class Bostedsadresse(
    val angittFlyttedato: LocalDate?,
    val gyldigFraOgMed: LocalDateTime?,
    val gyldigTilOgMed: LocalDateTime?,
    val coAdressenavn: String?,
    val vegadresse: Vegadresse?,
    val matrikkeladresse: Matrikkeladresse?,
    val utenlandskAdresse: UtenlandskAdresse?,
    val ukjentBosted: UkjentBosted?
)

data class Kontaktadresse(
    val gyldigFraOgMed: LocalDateTime?,
    val gyldigTilOgMed: LocalDateTime?,
    val type: KontaktadresseType,
    val coAdressenavn: String?,
    val postboksadresse: Postboksadresse?,
    val vegadresse: Vegadresse?,
    val postadresseIFrittFormat: PostadresseIFrittFormat?,
    val utenlandskAdresse: UtenlandskAdresse?,
    val utenlandskAdresseIFrittFormat: UtenlandskAdresseIFrittFormat?
)

enum class KontaktadresseType {
    Innland, Utland
}

data class Oppholdsadresse(
    val gyldigFraOgMed: LocalDateTime?,
    val gyldigTilOgMed: LocalDateTime?,
    val coAdressenavn: String?,
    val utenlandskAdresse: UtenlandskAdresse?,
    val vegadresse: Vegadresse?,
    val matrikkeladresse: Matrikkeladresse?,
    val oppholdAnnetSted: String?
)

data class PostadresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postnummer: String?
)

data class Postboksadresse(
    val postbokseier: String?,
    val postboks: String,
    val postnummer: String?
)

data class UtenlandskAdresse(
    val adressenavnNummer: String?,
    val bygningEtasjeLeilighet: String?,
    val postboksNummerNavn: String?,
    val postkode: String?,
    val bySted: String?,
    val regionDistriktOmraade: String?,
    val landkode: String
)

data class UtenlandskAdresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postkode: String?,
    val byEllerStedsnavn: String?,
    val landkode: String
)

data class Vegadresse(
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

data class Matrikkeladresse(
    val matrikkelId: Long?,
    val bruksenhetsnummer: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val kommunenummer: String?
)

data class UkjentBosted(
    val bostedskommune: String?
)
