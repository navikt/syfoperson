package no.nav.syfo.client.krr

import no.nav.syfo.person.api.domain.syfomodiaperson.SyfomodiapersonKontaktinfo
import java.io.Serializable

data class DigitalKontaktinfoBolk(
    val feil: Map<String, String>? = null,
    val personer: Map<String, DigitalKontaktinfo>? = null
) : Serializable

data class DigitalKontaktinfo(
    val epostadresse: String? = null,
    val aktiv: Boolean,
    val kanVarsles: Boolean? = null,
    val reservert: Boolean? = null,
    val mobiltelefonnummer: String? = null,
    val personident: String,
) : Serializable

fun DigitalKontaktinfo.toSyfomodiapersonKontaktinfo() =
    SyfomodiapersonKontaktinfo(
        fnr = this.personident,
        epost = this.epostadresse,
        tlf = this.mobiltelefonnummer,
        skalHaVarsel = this.kanVarsles ?: false,
    )
