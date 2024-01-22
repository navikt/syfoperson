package no.nav.syfo.person.api.domain

import no.nav.syfo.client.kodeverk.Postinformasjon
import no.nav.syfo.client.kodeverk.getPoststedByPostnummer
import no.nav.syfo.client.pdl.*

data class PersonAdresseResponse(
    val navn: String,
    val bostedsadresse: Bostedsadresse?,
    val kontaktadresse: Kontaktadresse?,
    val oppholdsadresse: Oppholdsadresse?,
)

data class Bostedsadresse(
    val vegadresse: Vegadresse?,
    val matrikkeladresse: Matrikkeladresse?,
    val utenlandskAdresse: UtenlandskAdresse?,
    val ukjentBosted: UkjentBosted?,
) {
    constructor(pdlBostedsadresse: PdlBostedsadresse, postinformasjonList: List<Postinformasjon>) : this(
        vegadresse = pdlBostedsadresse.vegadresse?.let { Vegadresse(it, postinformasjonList) },
        matrikkeladresse = pdlBostedsadresse.matrikkeladresse?.let { Matrikkeladresse(it, postinformasjonList) },
        utenlandskAdresse = pdlBostedsadresse.utenlandskAdresse?.let { UtenlandskAdresse(it) },
        ukjentBosted = pdlBostedsadresse.ukjentBosted?.let { UkjentBosted(it) },
    )
}

data class Kontaktadresse(
    val type: KontaktadresseType,
    val postboksadresse: Postboksadresse?,
    val vegadresse: Vegadresse?,
    val postadresseIFrittFormat: PostadresseIFrittFormat?,
    val utenlandskAdresse: UtenlandskAdresse?,
    val utenlandskAdresseIFrittFormat: UtenlandskAdresseIFrittFormat?,
) {
    constructor(pdlKontaktadresse: PdlKontaktadresse, postinformasjonList: List<Postinformasjon>) : this(
        type = KontaktadresseType.valueOf(pdlKontaktadresse.type.name),
        postboksadresse = pdlKontaktadresse.postboksadresse?.let { Postboksadresse(it, postinformasjonList) },
        vegadresse = pdlKontaktadresse.vegadresse?.let { Vegadresse(it, postinformasjonList) },
        postadresseIFrittFormat = pdlKontaktadresse.postadresseIFrittFormat?.let {
            PostadresseIFrittFormat(
                it,
                postinformasjonList
            )
        },
        utenlandskAdresse = pdlKontaktadresse.utenlandskAdresse?.let { UtenlandskAdresse(it) },
        utenlandskAdresseIFrittFormat = pdlKontaktadresse.utenlandskAdresseIFrittFormat?.let {
            UtenlandskAdresseIFrittFormat(
                it
            )
        },
    )
}

enum class KontaktadresseType {
    Innland, Utland
}

data class Oppholdsadresse(
    val utenlandskAdresse: UtenlandskAdresse?,
    val vegadresse: Vegadresse?,
    val matrikkeladresse: Matrikkeladresse?,
    val oppholdAnnetSted: String?,
) {
    constructor(pdlOppholdsadresse: PdlOppholdsadresse, postinformasjonList: List<Postinformasjon>) : this(
        utenlandskAdresse = pdlOppholdsadresse.utenlandskAdresse?.let { UtenlandskAdresse(it) },
        vegadresse = pdlOppholdsadresse.vegadresse?.let { Vegadresse(it, postinformasjonList) },
        matrikkeladresse = pdlOppholdsadresse.matrikkeladresse?.let { Matrikkeladresse(it, postinformasjonList) },
        oppholdAnnetSted = pdlOppholdsadresse.oppholdAnnetSted,
    )
}

data class PostadresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postnummer: String?,
    val poststed: String?,
) {
    constructor(
        pdlPostadresseIFrittFormat: PdlPostadresseIFrittFormat,
        postinformasjonList: List<Postinformasjon>,
    ) : this(
        adresselinje1 = pdlPostadresseIFrittFormat.adresselinje1,
        adresselinje2 = pdlPostadresseIFrittFormat.adresselinje2,
        adresselinje3 = pdlPostadresseIFrittFormat.adresselinje3,
        postnummer = pdlPostadresseIFrittFormat.postnummer,
        poststed = postinformasjonList.getPoststedByPostnummer(pdlPostadresseIFrittFormat.postnummer),
    )
}

data class Postboksadresse(
    val postbokseier: String?,
    val postboks: String,
    val postnummer: String?,
    val poststed: String?,
) {
    constructor(pdlPostboksadresse: PdlPostboksadresse, postinformasjonList: List<Postinformasjon>) : this(
        postbokseier = pdlPostboksadresse.postbokseier,
        postboks = pdlPostboksadresse.postboks,
        postnummer = pdlPostboksadresse.postnummer,
        poststed = postinformasjonList.getPoststedByPostnummer(pdlPostboksadresse.postnummer),
    )
}

data class UtenlandskAdresse(
    val adressenavnNummer: String?,
    val bygningEtasjeLeilighet: String?,
    val postboksNummerNavn: String?,
    val postkode: String?,
    val bySted: String?,
    val regionDistriktOmraade: String?,
    val landkode: String,
) {
    constructor(pdlUtenlandskAdresse: PdlUtenlandskAdresse) : this(
        adressenavnNummer = pdlUtenlandskAdresse.adressenavnNummer,
        bygningEtasjeLeilighet = pdlUtenlandskAdresse.bygningEtasjeLeilighet,
        postboksNummerNavn = pdlUtenlandskAdresse.postboksNummerNavn,
        postkode = pdlUtenlandskAdresse.postkode,
        bySted = pdlUtenlandskAdresse.bySted,
        regionDistriktOmraade = pdlUtenlandskAdresse.regionDistriktOmraade,
        landkode = pdlUtenlandskAdresse.landkode,
    )
}

data class UtenlandskAdresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postkode: String?,
    val byEllerStedsnavn: String?,
    val landkode: String,
) {
    constructor(pdlUtenlandskAdresseIFrittFormat: PdlUtenlandskAdresseIFrittFormat) : this(
        adresselinje1 = pdlUtenlandskAdresseIFrittFormat.adresselinje1,
        adresselinje2 = pdlUtenlandskAdresseIFrittFormat.adresselinje2,
        adresselinje3 = pdlUtenlandskAdresseIFrittFormat.adresselinje3,
        postkode = pdlUtenlandskAdresseIFrittFormat.postkode,
        byEllerStedsnavn = pdlUtenlandskAdresseIFrittFormat.byEllerStedsnavn,
        landkode = pdlUtenlandskAdresseIFrittFormat.landkode,
    )
}

data class Vegadresse(
    val husnummer: String?,
    val husbokstav: String?,
    val adressenavn: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val poststed: String?,
) {
    constructor(pdlVegadresse: PdlVegadresse, postinformasjonListe: List<Postinformasjon>) : this(
        husnummer = pdlVegadresse.husnummer,
        husbokstav = pdlVegadresse.husbokstav,
        adressenavn = pdlVegadresse.adressenavn,
        tilleggsnavn = pdlVegadresse.tilleggsnavn,
        postnummer = pdlVegadresse.postnummer,
        poststed = postinformasjonListe.getPoststedByPostnummer(pdlVegadresse.postnummer),
    )
}

data class Matrikkeladresse(
    val bruksenhetsnummer: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val poststed: String?,
) {
    constructor(pdlMatrikkeladresse: PdlMatrikkeladresse, postinformasjonListe: List<Postinformasjon>) : this(
        bruksenhetsnummer = pdlMatrikkeladresse.bruksenhetsnummer,
        tilleggsnavn = pdlMatrikkeladresse.tilleggsnavn,
        postnummer = pdlMatrikkeladresse.postnummer,
        poststed = postinformasjonListe.getPoststedByPostnummer(pdlMatrikkeladresse.postnummer),
    )
}

data class UkjentBosted(
    val bostedskommune: String?,
) {
    constructor(pdlUkjentBosted: PdlUkjentBosted) : this(
        bostedskommune = pdlUkjentBosted.bostedskommune,
    )
}
