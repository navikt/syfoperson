package no.nav.syfo.testhelper

import no.nav.syfo.client.pdl.*
import java.time.LocalDate

fun generatePdlPersonNavn(): PdlPersonNavn {
    return PdlPersonNavn(
        fornavn = UserConstants.PERSON_NAME_FIRST,
        mellomnavn = UserConstants.PERSON_NAME_MIDDLE,
        etternavn = UserConstants.PERSON_NAME_LAST
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

fun generatePdlPersonResponse(
    gradering: Gradering? = null,
    doedsdato: LocalDate? = null,
    tilrettelagtKommunikasjon: PdlTilrettelagtKommunikasjon? = null,
    sikkerhetstiltak: PdlSikkerhetstiltak? = null,
) = PdlPersonResponse(
    errors = null,
    data = generatePdlHentPerson(
        pdlPersonNavn = generatePdlPersonNavn(),
        adressebeskyttelse = generateAdressebeskyttelse(gradering = gradering),
        doedsdato = doedsdato,
        tilrettelagtKommunikasjon = tilrettelagtKommunikasjon,
        sikkerhetstiltak = if (sikkerhetstiltak == null) emptyList() else {
            listOf(sikkerhetstiltak)
        },
    )
)

fun generatePdlPersonResponseError() = PdlPersonResponse(
    errors = null,
    data = null,
)

fun generateAdressebeskyttelse(
    gradering: Gradering? = null
): Adressebeskyttelse {
    return Adressebeskyttelse(
        gradering = gradering ?: Gradering.UGRADERT
    )
}

fun generatePdlHentPerson(
    pdlPersonNavn: PdlPersonNavn?,
    adressebeskyttelse: Adressebeskyttelse? = null,
    doedsdato: LocalDate? = null,
    tilrettelagtKommunikasjon: PdlTilrettelagtKommunikasjon? = null,
    sikkerhetstiltak: List<PdlSikkerhetstiltak>,
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
            ),
            doedsfall = if (doedsdato == null) emptyList() else {
                listOf(PdlDoedsfall(doedsdato))
            },
            tilrettelagtKommunikasjon = listOfNotNull(tilrettelagtKommunikasjon),
            sikkerhetstiltak = sikkerhetstiltak,
        )
    )
}

fun generatePdlTilrettelagtKommunikasjon(): PdlTilrettelagtKommunikasjon =
    PdlTilrettelagtKommunikasjon(
        talespraaktolk = PdlSprak(spraak = "NO"),
        tegnspraaktolk = PdlSprak(spraak = null),
    )

fun generatePdlSikkerhetsiltak(): PdlSikkerhetstiltak = PdlSikkerhetstiltak(
    tiltakstype = SikkerhetstiltaksType.FYUS,
    beskrivelse = "Fysisk utestengelse",
    gyldigFraOgMed = LocalDate.now().minusWeeks(1),
    gyldigTilOgMed = LocalDate.now().plusWeeks(3),
)
