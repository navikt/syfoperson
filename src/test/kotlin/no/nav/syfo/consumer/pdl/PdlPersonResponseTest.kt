package no.nav.syfo.consumer.pdl

import no.nav.syfo.client.pdl.Adressebeskyttelse
import no.nav.syfo.client.pdl.Gradering
import no.nav.syfo.client.pdl.PdlPersonNavn
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.generatePdlHentPerson
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PdlPersonResponseTest {

    @Test
    fun fullName() {
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
        assertEquals(expected, result)
    }

    @Test
    fun `fullName with no middle name`() {
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
        assertEquals(expected, result)
    }

    @Test
    fun `getDoedsdato when set`() {
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
        assertEquals(LocalDate.now(), result)
    }

    @Test
    fun `getDoedsdato when not set`() {
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
        assertNull(result)
    }

    @Test
    fun `isKode6Or7 is true with FORTROLIG`() {
        val pdlPersonResponse = generatePdlHentPerson(
            pdlPersonNavn = null,
            personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
            adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.FORTROLIG),
            doedsdato = null,
            tilrettelagtKommunikasjon = null,
            sikkerhetstiltak = emptyList(),
        ).copy()
        val result = pdlPersonResponse.hentPerson?.isKode6Or7
        val expected = true
        assertEquals(expected, result)
    }

    @Test
    fun `isKode6Or7 is true with STRENGT_FORTROLIG`() {
        val pdlPersonResponse = generatePdlHentPerson(
            pdlPersonNavn = null,
            personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
            adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.STRENGT_FORTROLIG),
            doedsdato = null,
            tilrettelagtKommunikasjon = null,
            sikkerhetstiltak = emptyList(),
        ).copy()
        val result = pdlPersonResponse.hentPerson?.isKode6Or7
        val expected = true
        assertEquals(expected, result)
    }

    @Test
    fun `isKode6Or7 is true with STRENGT_FORTROLIG_UTLAND`() {
        val pdlPersonResponse = generatePdlHentPerson(
            pdlPersonNavn = null,
            personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
            adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.STRENGT_FORTROLIG_UTLAND),
            doedsdato = null,
            tilrettelagtKommunikasjon = null,
            sikkerhetstiltak = emptyList(),
        )
        val result = pdlPersonResponse.hentPerson?.isKode6Or7
        val expected = true
        assertEquals(expected, result)
    }

    @Test
    fun `isKode6Or7 returns false with UGRADERT`() {
        val pdlPersonResponse = generatePdlHentPerson(
            pdlPersonNavn = null,
            personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
            adressebeskyttelse = Adressebeskyttelse(gradering = Gradering.UGRADERT),
            doedsdato = null,
            tilrettelagtKommunikasjon = null,
            sikkerhetstiltak = emptyList(),
        )
        val result = pdlPersonResponse.hentPerson?.isKode6Or7
        val expected = false
        assertEquals(expected, result)
    }
}
