package no.nav.syfo.consumer.dkif

import no.nav.syfo.person.api.domain.syfomodiaperson.SyfomodiapersonKontaktinfo
import java.io.Serializable

data class DigitalKontaktinfoBolk(
    val feil: Map<String, Feil>? = null,
    val kontaktinfo: Map<String, DigitalKontaktinfo>? = null
) : Serializable

data class DigitalKontaktinfo(
    val epostadresse: String? = null,
    val kanVarsles: Boolean,
    val reservert: Boolean? = null,
    val mobiltelefonnummer: String? = null,
    val personident: String
) : Serializable

fun DigitalKontaktinfo.toSyfomodiapersonKontaktinfo() =
    SyfomodiapersonKontaktinfo(
        fnr = this.personident,
        epost = this.epostadresse,
        tlf = this.mobiltelefonnummer,
        skalHaVarsel = this.kanVarsles,
    )

data class Feil(
    val melding: String
) : Serializable
