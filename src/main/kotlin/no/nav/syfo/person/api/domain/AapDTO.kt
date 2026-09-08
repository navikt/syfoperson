package no.nav.syfo.person.api.domain

import java.time.LocalDate
import no.nav.syfo.client.aap.AapPeriode
import no.nav.syfo.client.aap.AapSak
import no.nav.syfo.client.aap.AapSakerResponse
import no.nav.syfo.client.aap.AapVedtak
import no.nav.syfo.client.aap.Kilde
import no.nav.syfo.client.aap.AapSoknadStatus
import no.nav.syfo.util.isAfterOrEqual
import no.nav.syfo.util.isBeforeOrEqual

data class AapRequest(
    val personident: String,
)

data class AapSakerDTO(
    val soknader: List<AapSoknadDTO>,
    val vedtak: List<AapVedtakDTO>,
) {
    companion object {
        fun fromSaker(
            response: AapSakerResponse,
            today: LocalDate = LocalDate.now(),
        ): AapSakerDTO = AapSakerDTO(
            soknader = response.saker
                .filter { it.kilde == Kilde.KELVIN }
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
    val statuskode: AapSoknadStatus,
    val erAktiv: Boolean,
) {
    companion object {
        fun fromSak(sak: AapSak): AapSoknadDTO = AapSoknadDTO(
            sakid = sak.sakid,
            soknadsdatoer = sak.soknadsdatoer,
            statuskode = sak.statuskode,
            erAktiv = sak.statuskode == AapSoknadStatus.SOKNAD_UNDER_BEHANDLING,
        )
    }
}

data class AapVedtakDTO(
    val sakid: String,
    val kilde: Kilde,
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
            erAktivt = vedtak.perioder
                .filter { it.tilOgMedDato != null } // Se https://nav-it.slack.com/archives/C0312J501GX/p1788870062686759?thread_ts=1740730504.702299&cid=C0312J501GX
                .any { it.erAktiv(today) },
        )
    }
}

// Det er kun Arena som har null-verdier for disse, Kelvin har alltid fraOgMedDato og tilOgMedDato.
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

private fun AapPeriode.erAktiv(today: LocalDate): Boolean =
    this.fraOgMedDato != null &&
        this.tilOgMedDato != null &&
        this.fraOgMedDato.isBeforeOrEqual(today) &&
        this.tilOgMedDato.isAfterOrEqual(today)
