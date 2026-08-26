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
    /**
     * Arena: AVSLU, FORDE, GODKJ, INNST, IVERK, KONT, MOTAT, OPPRE, REGIS, UKJENT.
     * Kelvin: OPPRETTET, UTREDES, LØPENDE, AVSLUTTET, SOKNAD_UNDER_BEHANDLING,
     * REVURDERING_UNDER_BEHANDLING, FERDIGBEHANDLET.
     */
    val statuskode: String,
    val soknadsdatoer: List<LocalDate> = emptyList(),
    val vedtak: List<AapVedtak>,
    /** Possible values: ARENA, KELVIN. */
    val kilde: String,
)

data class AapVedtak(
    val vedtaksdato: LocalDate,
    val perioder: List<AapPeriode>,
)

data class AapPeriode(
    val fraOgMedDato: LocalDate?,
    val tilOgMedDato: LocalDate?,
)
