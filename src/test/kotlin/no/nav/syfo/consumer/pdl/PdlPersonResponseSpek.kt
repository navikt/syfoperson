package no.nav.syfo.consumer.pdl

import no.nav.syfo.client.pdl.*
import no.nav.syfo.person.api.PersonNavnApiSpek
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.generatePdlHentPerson
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PdlPersonResponseSpek : Spek({
    describe(PersonNavnApiSpek::class.java.simpleName) {

        it("getFullName") {
            val pdlPersonResponse = generatePdlHentPerson(
                PdlPersonNavn(
                    UserConstants.PERSON_NAME_FIRST,
                    UserConstants.PERSON_NAME_MIDDLE,
                    UserConstants.PERSON_NAME_LAST,
                ),
                null,
            ).copy()
            val result = pdlPersonResponse.getFullName()
            val expected =
                "${UserConstants.PERSON_NAME_FIRST} ${UserConstants.PERSON_NAME_MIDDLE} ${UserConstants.PERSON_NAME_LAST}"
            result shouldBeEqualTo expected
        }

        it("getFullName with no middle name") {
            val pdlPersonResponse = generatePdlHentPerson(
                PdlPersonNavn(
                    UserConstants.PERSON_NAME_FIRST,
                    null,
                    UserConstants.PERSON_NAME_LAST,
                ),
                null,
            ).copy()
            val result = pdlPersonResponse.getFullName()
            val expected = "${UserConstants.PERSON_NAME_FIRST} ${UserConstants.PERSON_NAME_LAST}"
            result shouldBeEqualTo expected
        }

        it("isKode6Or7 is true with ${Gradering.FORTROLIG}") {
            val pdlPersonResponse = generatePdlHentPerson(
                null,
                Adressebeskyttelse(gradering = Gradering.FORTROLIG),
            ).copy()
            val result = pdlPersonResponse.isKode6Or7()
            val expected = true
            result shouldBeEqualTo expected
        }

        it("isKode6Or7 is true with ${Gradering.STRENGT_FORTROLIG}") {
            val pdlPersonResponse = generatePdlHentPerson(
                null,
                Adressebeskyttelse(gradering = Gradering.STRENGT_FORTROLIG),
            ).copy()
            val result = pdlPersonResponse.isKode6Or7()
            val expected = true
            result shouldBeEqualTo expected
        }

        it("isKode6Or7 is true with ${Gradering.STRENGT_FORTROLIG_UTLAND}") {
            val pdlPersonResponse = generatePdlHentPerson(
                null,
                Adressebeskyttelse(gradering = Gradering.STRENGT_FORTROLIG_UTLAND),
            ).copy()
            val result = pdlPersonResponse.isKode6Or7()
            val expected = true
            result shouldBeEqualTo expected
        }

        it("isKode6Or7 returns false with ${Gradering.UGRADERT}") {
            val pdlPersonResponse = generatePdlHentPerson(
                null,
                Adressebeskyttelse(gradering = Gradering.UGRADERT),
            ).copy()
            val result = pdlPersonResponse.isKode6Or7()
            val expected = false
            result shouldBeEqualTo expected
        }
    }
})
