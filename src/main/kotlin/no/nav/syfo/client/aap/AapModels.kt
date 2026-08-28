package no.nav.syfo.client.aap

import java.time.LocalDate

data class AapSakerRequest(
    val personidentifikator: String,
)

data class AapSakerResponse(
    val saker: List<AapSak>,
)

data class AapSak(
    val sakid: String,
    val statuskode: AapSoknadStatus,
    val soknadsdatoer: List<LocalDate> = emptyList(),
    val vedtak: List<AapVedtak>,
    val kilde: Kilde,
)

data class AapVedtak(
    val vedtaksdato: LocalDate,
    val perioder: List<AapPeriode>,
)

data class AapPeriode(
    val fraOgMedDato: LocalDate?,
    val tilOgMedDato: LocalDate?,
)

enum class Kilde {
    ARENA,
    KELVIN
}

enum class AapSoknadStatus {
    // Arena, ikke interessante for oss
    AVSLU,
    FORDE,
    GODKJ,
    INNST,
    IVERK,
    KONT,
    MOTAT,
    OPPRE,
    REGIS,
    UKJENT,

    // Disse skal bort fra Kelvin
    OPPRETTET,
    UTREDES,
    LØPENDE,
    AVSLUTTET,

    // Disse kommer fra Kelvin og brukes i de nye sakene
    SOKNAD_UNDER_BEHANDLING,
    REVURDERING_UNDER_BEHANDLING,
    FERDIGBEHANDLET,
}
