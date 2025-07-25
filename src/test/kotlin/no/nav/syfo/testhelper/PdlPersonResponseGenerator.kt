package no.nav.syfo.testhelper

import no.nav.syfo.client.pdl.*
import no.nav.syfo.domain.PersonIdentNumber
import java.time.LocalDate
import java.time.LocalDateTime

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

fun generatePdlBostedsadress(): PdlBostedsadresse {
    return PdlBostedsadresse(
        angittFlyttedato = null,
        gyldigFraOgMed = LocalDateTime.now().minusDays(1),
        gyldigTilOgMed = null,
        coAdressenavn = null,
        vegadresse = generatePdlVegadresse(),
        matrikkeladresse = null,
        utenlandskAdresse = null,
        ukjentBosted = null
    )
}

fun generatePdlVegadresse(): PdlVegadresse {
    return PdlVegadresse(
        matrikkelId = null,
        husnummer = null,
        husbokstav = null,
        bruksenhetsnummer = null,
        adressenavn = null,
        kommunenummer = null,
        bydelsnummer = null,
        tilleggsnavn = null,
        postnummer = "1001",
    )
}

fun generatePdlKontaktadressee(): PdlKontaktadresse {
    return PdlKontaktadresse(
        gyldigFraOgMed = LocalDateTime.now().minusDays(1),
        gyldigTilOgMed = null,
        type = PdlKontaktadresseType.Innland,
        coAdressenavn = null,
        postboksadresse = null,
        vegadresse = generatePdlVegadresse(),
        postadresseIFrittFormat = null,
        utenlandskAdresse = null,
        utenlandskAdresseIFrittFormat = null
    )
}

fun generatePdlOppholdsadresse(): PdlOppholdsadresse {
    return PdlOppholdsadresse(
        gyldigFraOgMed = LocalDateTime.now().minusDays(1),
        gyldigTilOgMed = null,
        coAdressenavn = null,
        utenlandskAdresse = null,
        vegadresse = generatePdlVegadresse(),
        matrikkeladresse = null,
        oppholdAnnetSted = null
    )
}

fun generatePdlPersonResponse(
    personident: PersonIdentNumber,
    gradering: Gradering? = null,
    foedselsdato: LocalDate = LocalDate.now().minusYears(30),
    doedsdato: LocalDate? = null,
    tilrettelagtKommunikasjon: PdlTilrettelagtKommunikasjon? = null,
    sikkerhetstiltak: PdlSikkerhetstiltak? = null,
) = PdlPersonResponse(
    errors = null,
    data = generatePdlHentPerson(
        pdlPersonNavn = generatePdlPersonNavn(),
        personident = personident,
        adressebeskyttelse = generateAdressebeskyttelse(gradering = gradering),
        foedselsdato = foedselsdato,
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

fun generateFolkeregisteridentifikator(personident: PersonIdentNumber) =
    FolkeregisterIdentifikator(
        identifikasjonsnummer = personident.value,
        status = FolkeregisterIdentStatus.I_BRUK
    )

fun generatePdlHentPerson(
    pdlPersonNavn: PdlPersonNavn?,
    personident: PersonIdentNumber? = UserConstants.ARBEIDSTAKER_PERSONIDENT,
    adressebeskyttelse: Adressebeskyttelse? = null,
    foedselsdato: LocalDate = LocalDate.now().minusYears(30),
    doedsdato: LocalDate? = null,
    tilrettelagtKommunikasjon: PdlTilrettelagtKommunikasjon? = null,
    sikkerhetstiltak: List<PdlSikkerhetstiltak>,
): PdlHentPerson {
    return PdlHentPerson(
        hentPerson = PdlPerson(
            navn = listOf(
                pdlPersonNavn ?: generatePdlPersonNavn()
            ),
            folkeregisteridentifikator = listOf(
                generateFolkeregisteridentifikator(personident!!),
            ),
            adressebeskyttelse = listOf(
                adressebeskyttelse ?: generateAdressebeskyttelse()
            ),
            bostedsadresse = listOf(
                generatePdlBostedsadress()
            ),
            kontaktadresse = listOf(
                generatePdlKontaktadressee()
            ),
            oppholdsadresse = listOf(
                generatePdlOppholdsadresse()
            ),
            doedsfall = if (doedsdato == null) emptyList() else {
                listOf(PdlDoedsfall(doedsdato))
            },
            foedselsdato = listOf(PdlFoedselsdato(foedselsdato = foedselsdato, foedselsaar = foedselsdato.year)),
            kjoenn = listOf(PdlKjoenn("KVINNE")),
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
