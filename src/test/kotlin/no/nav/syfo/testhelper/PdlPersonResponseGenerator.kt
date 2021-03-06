package no.nav.syfo.testhelper

import no.nav.syfo.consumer.pdl.*

fun generatePdlPersonNavn(): PdlPersonNavn {
    return PdlPersonNavn(
        fornavn = UserConstants.ARBEIDSTAKER_NAME_FIRST,
        mellomnavn = UserConstants.ARBEIDSTAKER_NAME_MIDDLE,
        etternavn = UserConstants.ARBEIDSTAKER_NAME_LAST
    )
}

fun generateAdressebeskyttelse(): Adressebeskyttelse {
    return Adressebeskyttelse(
        gradering = Gradering.UGRADERT
    )
}

fun generateBostedsadress(): Bostedsadresse {
    return Bostedsadresse(
        angittFlyttedato = null,
        gyldigFraOgMed = null,
        gyldigTilOgMed = null,
        coAdressenavn = null,
        vegadresse = null,
        matrikkeladresse = null,
        utenlandskAdresse = null,
        ukjentBosted = null
    )
}

fun generateKontaktadressee(): Kontaktadresse {
    return Kontaktadresse(
        gyldigFraOgMed = null,
        gyldigTilOgMed = null,
        type = KontaktadresseType.Innland,
        coAdressenavn = null,
        postboksadresse = null,
        vegadresse = null,
        postadresseIFrittFormat = null,
        utenlandskAdresse = null,
        utenlandskAdresseIFrittFormat = null
    )
}

fun generateOppholdsadresse(): Oppholdsadresse {
    return Oppholdsadresse(
        gyldigFraOgMed = null,
        gyldigTilOgMed = null,
        coAdressenavn = null,
        utenlandskAdresse = null,
        vegadresse = null,
        matrikkeladresse = null,
        oppholdAnnetSted = null
    )
}

fun generatePdlHentPerson(
    pdlPersonNavn: PdlPersonNavn?,
    adressebeskyttelse: Adressebeskyttelse?
): PdlHentPerson {
    return PdlHentPerson(
        hentPerson = PdlPerson(
            navn = listOf(
                pdlPersonNavn ?: generatePdlPersonNavn()
            ),
            adressebeskyttelse = listOf(
                adressebeskyttelse ?: generateAdressebeskyttelse()
            ),
            bostedsadresse = listOf(
                generateBostedsadress()
            ),
            kontaktadresse = listOf(
                generateKontaktadressee()
            ),
            oppholdsadresse = listOf(
                generateOppholdsadresse()
            )
        )
    )
}
