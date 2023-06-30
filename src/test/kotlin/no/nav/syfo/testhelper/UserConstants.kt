package no.nav.syfo.testhelper

import no.nav.syfo.domain.*

object UserConstants {
    val ARBEIDSTAKER_PERSONIDENT = PersonIdentNumber("12345678912")
    val ARBEIDSTAKER_AKTORID = AktorId("10" + ARBEIDSTAKER_PERSONIDENT.value)
    val ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT = PersonIdentNumber("12345678913")

    val ARBEIDSTAKER_VEILEDER_NO_ACCESS = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value.replace("2", "1"))
    val ARBEIDSTAKER_ADRESSEBESKYTTET = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value.replace("2", "6"))
    val ARBEIDSTAKER_DOD = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value.replace("2", "7"))
    val ARBEIDSTAKER_TILRETTELAGT_KOMMUNIKASJON = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value.replace("2", "8"))

    const val PERSON_TLF = "12345678"
    const val PERSON_EMAIL = "test@nav.no"

    const val PERSON_NAME_FIRST = "First"
    const val PERSON_NAME_MIDDLE = "Middle"
    const val PERSON_NAME_LAST = "Last"

    val VIRKSOMHETSNUMMER_DEFAULT = Virksomhetsnummer("912345678")

    const val VEILEDER_IDENT = "Z999999"
}
