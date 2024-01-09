package no.nav.syfo.person.api.domain.syfomodiaperson

import java.time.LocalDate

data class Sikkerhetstiltak(val type: String, val beskrivelse: String, val gyldigFom: LocalDate, val gyldigTom: LocalDate)
