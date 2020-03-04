package no.nav.syfo.pdl

import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.generatePdlHentPerson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PdlResponseTest {

    @Test
    fun getFullName() {
        val pdlPersonResponse = generatePdlHentPerson(
                PdlPersonNavn(
                        UserConstants.ARBEIDSTAKER_NAME_FIRST,
                        UserConstants.ARBEIDSTAKER_NAME_MIDDLE,
                        UserConstants.ARBEIDSTAKER_NAME_LAST
                ),
                null
        ).copy()
        val result = pdlPersonResponse.getName()
        assertThat(result).isEqualTo("${UserConstants.ARBEIDSTAKER_NAME_FIRST} ${UserConstants.ARBEIDSTAKER_NAME_MIDDLE} ${UserConstants.ARBEIDSTAKER_NAME_LAST}")
    }

    @Test
    fun getFullNameNoMiddleName() {
        val pdlPersonResponse = generatePdlHentPerson(
                PdlPersonNavn(
                        UserConstants.ARBEIDSTAKER_NAME_FIRST,
                        null,
                        UserConstants.ARBEIDSTAKER_NAME_LAST
                ),
                null
        ).copy()
        val result = pdlPersonResponse.getName()
        assertThat(result).isEqualTo("${UserConstants.ARBEIDSTAKER_NAME_FIRST} ${UserConstants.ARBEIDSTAKER_NAME_LAST}")
    }

    @Test
    fun isKode6Or7WithGraderringFortrolig() {
        val pdlPersonResponse = generatePdlHentPerson(
                null,
                Adressebeskyttelse(gradering = Gradering.FORTROLIG)
        ).copy()
        val result = pdlPersonResponse.isKode6Or7()
        assertThat(result).isTrue()
    }

    @Test
    fun isKode6Or7WithGraderingStrengtFortrolig() {
        val pdlPersonResponse = generatePdlHentPerson(
                null,
                Adressebeskyttelse(gradering = Gradering.STRENGT_FORTROLIG)
        ).copy()
        val result = pdlPersonResponse.isKode6Or7()
        assertThat(result).isTrue()
    }

    @Test
    fun isKode6Or7WithGraderingStrengtFortroligUtland() {
        val pdlPersonResponse = generatePdlHentPerson(
                null,
                Adressebeskyttelse(gradering = Gradering.STRENGT_FORTROLIG_UTLAND)
        ).copy()
        val result = pdlPersonResponse.isKode6Or7()
        assertThat(result).isTrue()
    }

    @Test
    fun isNotKode6Or7WithGraderingUgradert() {
        val pdlPersonResponse = generatePdlHentPerson(
                null,
                Adressebeskyttelse(gradering = Gradering.UGRADERT)
        ).copy()
        val result = pdlPersonResponse.isKode6Or7()
        assertThat(result).isFalse()
    }
}
