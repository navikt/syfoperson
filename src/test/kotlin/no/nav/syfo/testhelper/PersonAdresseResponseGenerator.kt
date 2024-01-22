package no.nav.syfo.testhelper

import no.nav.syfo.person.api.domain.*

fun generatePersonAdresseResponse(): PersonAdresseResponse {
    return PersonAdresseResponse(
        navn = "First Middle Last",
        bostedsadresse = generateBostedsadresse(),
        kontaktadresse = generateKontaktAdresse(),
        oppholdsadresse = generateOppholdsadresse(),
    )
}

fun generateBostedsadresse(): Bostedsadresse {
    return Bostedsadresse(
        vegadresse = generateVegadresse(),
        matrikkeladresse = null,
        utenlandskAdresse = null,
        ukjentBosted = null,
    )
}

fun generateKontaktAdresse(): Kontaktadresse {
    return Kontaktadresse(
        type = KontaktadresseType.Innland,
        postboksadresse = null,
        vegadresse = generateVegadresse(),
        postadresseIFrittFormat = null,
        utenlandskAdresse = null,
        utenlandskAdresseIFrittFormat = null,
    )
}

fun generateOppholdsadresse(): Oppholdsadresse {
    return Oppholdsadresse(
        utenlandskAdresse = null,
        vegadresse = generateVegadresse(),
        matrikkeladresse = null,
        oppholdAnnetSted = null,
    )
}

fun generateVegadresse(): Vegadresse {
    return Vegadresse(
        husnummer = null,
        husbokstav = null,
        adressenavn = null,
        postnummer = "1001",
        poststed = "OSLO",
        tilleggsnavn = null,
    )
}
