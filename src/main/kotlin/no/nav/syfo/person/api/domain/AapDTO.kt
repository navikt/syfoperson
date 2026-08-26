package no.nav.syfo.person.api.domain

import java.time.LocalDate
import no.nav.syfo.client.aap.AapPeriode
import no.nav.syfo.client.aap.AapSak
import no.nav.syfo.client.aap.AapSakerResponse
import no.nav.syfo.client.aap.AapVedtak

data class AapRequest(
    val personident: String,
)

data class AapStatusDTO(
    val soknader: List<AapSoknadDTO>,
    val vedtak: List<AapVedtakDTO>,
) {
    companion object {
        fun fromSaker(
            response: AapSakerResponse,
            today: LocalDate = LocalDate.now(),
        ): AapStatusDTO = AapStatusDTO(
            soknader = response.saker
                .filter { it.kilde == KILDE_KELVIN }
                .map { AapSoknadDTO.fromSak(it) },
            vedtak = response.saker.flatMap { sak ->
                sak.vedtak.map { vedtak ->
                    AapVedtakDTO.fromVedtak(
                        sak = sak,
                        vedtak = vedtak,
                        today = today,
                    )
                }
            },
        )
    }
}

data class AapSoknadDTO(
    val sakid: String,
    val soknadsdatoer: List<LocalDate>,
    val statuskode: String,
    val erAktiv: Boolean,
) {
    companion object {
        fun fromSak(sak: AapSak): AapSoknadDTO = AapSoknadDTO(
            sakid = sak.sakid,
            soknadsdatoer = sak.soknadsdatoer,
            statuskode = sak.statuskode,
            erAktiv = sak.statuskode == STATUS_SOKNAD_UNDER_BEHANDLING,
        )
    }
}

data class AapVedtakDTO(
    val sakid: String,
    val kilde: String,
    val vedtaksdato: LocalDate,
    val perioder: List<AapPeriodeDTO>,
    val erAktivt: Boolean,
) {
    companion object {
        fun fromVedtak(
            sak: AapSak,
            vedtak: AapVedtak,
            today: LocalDate,
        ): AapVedtakDTO = AapVedtakDTO(
            sakid = sak.sakid,
            kilde = sak.kilde,
            vedtaksdato = vedtak.vedtaksdato,
            perioder = vedtak.perioder.map { AapPeriodeDTO.fromPeriode(it) },
            erAktivt = vedtak.perioder.any { it.erAktiv(today) },
        )
    }
}

data class AapPeriodeDTO(
    val fraOgMedDato: LocalDate?,
    val tilOgMedDato: LocalDate?,
) {
    companion object {
        fun fromPeriode(periode: AapPeriode): AapPeriodeDTO = AapPeriodeDTO(
            fraOgMedDato = periode.fraOgMedDato,
            tilOgMedDato = periode.tilOgMedDato,
        )
    }
}

private fun AapPeriode.erAktiv(today: LocalDate): Boolean {
    val fraOgMedDato = fraOgMedDato
    return fraOgMedDato != null &&
        !fraOgMedDato.isAfter(today) &&
        (tilOgMedDato == null || !tilOgMedDato.isBefore(today))
}

private const val KILDE_KELVIN = "KELVIN"
private const val STATUS_SOKNAD_UNDER_BEHANDLING = "SOKNAD_UNDER_BEHANDLING"
