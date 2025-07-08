package no.nav.syfo.consumer.pdl

import no.nav.syfo.client.pdl.*
import no.nav.syfo.person.api.PersonNavnApiSpek
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.generatePdlHentPerson
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate

class PdlPersonResponseSpek : Spek({
    describe(PersonNavnApiSpek::class.java.simpleName) {

        it("fullName") {
            val pdlPersonResponse = generatePdlHentPerson(
                pdlPersonNavn = PdlPersonNavn(
                    UserConstants.PERSON_NAME_FIRST,
                    UserConstants.PERSON_NAME_MIDDLE,
                    UserConstants.PERSON_NAME_LAST,
                ),
                personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                adressebeskyttelse = null,
                doedsdato = null,
                tilrettelagtKommunikasjon = null,
                sikkerhetstiltak = emptyList(),
            )
            val result = pdlPersonResponse.hentPerson?.fullName
            val expected =
                "${UserConstants.PERSON_NAME_FIRST} ${UserConstants.PERSON_NAME_MIDDLE} ${UserConstants.PERSON_NAME_LAST}"
            result shouldBeEqualTo expected
        }

        it("fullName with no middle name") {
            val pdlPersonResponse = generatePdlHentPerson(
                pdlPersonNavn = PdlPersonNavn(
                    UserConstants.PERSON_NAME_FIRST,
                    null,
                    UserConstants.PERSON_NAME_LAST,
                ),
                personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                adressebeskyttelse = null,
                doedsdato = null,
                tilrettelagtKommunikasjon = null,
                sikkerhetstiltak = emptyList(),
            )
            val result = pdlPersonResponse.hentPerson?.fullName
            val expected = "${UserConstants.PERSON_NAME_FIRST} ${UserConstants.PERSON_NAME_LAST}"
            result shouldBeEqualTo expected
        }

        it("getDoedsdato when set") {
            val pdlPersonResponse = generatePdlHentPerson(
                pdlPersonNavn = PdlPersonNavn(
                    UserConstants.PERSON_NAME_FIRST,
                    UserConstants.PERSON_NAME_MIDDLE,
                    UserConstants.PERSON_NAME_LAST,
                ),
                personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                adressebeskyttelse = null,
                doedsdato = LocalDate.now(),
                tilrettelagtKommunikasjon = null,
                sikkerhetstiltak = emptyList(),
            )
            val result = pdlPersonResponse.hentPerson?.dodsdato
            result shouldBeEqualTo LocalDate.now()
        }

        it("getDoedsdato when not set") {
            val pdlPersonResponse = generatePdlHentPerson(
                pdlPersonNavn = PdlPersonNavn(
                    UserConstants.PERSON_NAME_FIRST,
                    UserConstants.PERSON_NAME_MIDDLE,
                    UserConstants.PERSON_NAME_LAST,
                ),
                personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                adressebeskyttelse = null,
                doedsdato = null,
                tilrettelagtKommunikasjon = null,
                sikkerhetstiltak = emptyList(),
            )
            val result = pdlPersonResponse.hentPerson?.dodsdato
            result shouldBe null
        }

        it("isKode6Or7 is true with ${Gradering.FORTROLIG}") {
            val pdlPersonResponse = generatePdlHentPerson(
                pdlPersonNavn = null,
                personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.FORTROLIG),
                doedsdato = LocalDate.now(),
                tilrettelagtKommunikasjon = null,
                sikkerhetstiltak = emptyList(),
            ).copy()
            val result = pdlPersonResponse.hentPerson?.isKode6Or7
            val expected = true
            result shouldBeEqualTo expected
        }

        it("isKode6Or7 is true with ${Gradering.STRENGT_FORTROLIG}") {
            val pdlPersonResponse = generatePdlHentPerson(
                pdlPersonNavn = null,
                personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.STRENGT_FORTROLIG),
                doedsdato = LocalDate.now(),
                tilrettelagtKommunikasjon = null,
                sikkerhetstiltak = emptyList(),
            ).copy()
            val result = pdlPersonResponse.hentPerson?.isKode6Or7
            val expected = true
            result shouldBeEqualTo expected
        }

        it("isKode6Or7 is true with ${Gradering.STRENGT_FORTROLIG_UTLAND}") {
            val pdlPersonResponse = generatePdlHentPerson(
                pdlPersonNavn = null,
                personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.STRENGT_FORTROLIG_UTLAND),
                doedsdato = LocalDate.now(),
                tilrettelagtKommunikasjon = null,
                sikkerhetstiltak = emptyList(),
            )
            val result = pdlPersonResponse.hentPerson?.isKode6Or7
            val expected = true
            result shouldBeEqualTo expected
        }

        it("isKode6Or7 returns false with ${Gradering.UGRADERT}") {
            val pdlPersonResponse = generatePdlHentPerson(
                pdlPersonNavn = null,
                personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.UGRADERT),
                doedsdato = LocalDate.now(),
                tilrettelagtKommunikasjon = null,
                sikkerhetstiltak = emptyList(),
            )
            val result = pdlPersonResponse.hentPerson?.isKode6Or7
            val expected = false
            result shouldBeEqualTo expected
        }
    }
})
