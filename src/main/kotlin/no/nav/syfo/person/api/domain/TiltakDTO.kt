package no.nav.syfo.person.api.domain

import java.time.LocalDate
import no.nav.syfo.client.tiltakshistorikk.TiltakshistorikkDeltakelse
import no.nav.syfo.client.tiltakshistorikk.TiltakshistorikkResponse

data class TiltakRequestDTO(
    val personident: String,
)

data class TiltakResponseDTO(
    val tiltak: List<TiltakDTO>,
) {
    companion object {
        fun fromHistorikk(
            response: TiltakshistorikkResponse,
            today: LocalDate = LocalDate.now(),
        ): TiltakResponseDTO = TiltakResponseDTO(
            tiltak = response.historikk
                .filter { it.erAktiv(today) }
                .distinctBy { it.id }
                .map { TiltakDTO.fromDeltakelse(it) },
        )
    }
}

data class TiltakDTO(
    val id: String,
    val tiltakskode: String,
    val tiltakstypeNavn: String,
    val startDato: LocalDate?,
    val sluttDato: LocalDate?,
) {
    companion object {
        fun fromDeltakelse(deltakelse: TiltakshistorikkDeltakelse): TiltakDTO = TiltakDTO(
            id = deltakelse.id,
            tiltakskode = deltakelse.tiltakstype.tiltakskode,
            tiltakstypeNavn = deltakelse.tiltakstype.navn,
            startDato = deltakelse.startDato,
            sluttDato = deltakelse.sluttDato,
        )
    }
}

private const val ARENA_STATUS_GJENNOMFORES = "GJENNOMFORES"
private const val TEAM_KOMET_STATUS_DELTAR = "DELTAR"
private const val TEAM_TILTAK_STATUS_GJENNOMFORES = "GJENNOMFORES"

private fun TiltakshistorikkDeltakelse.erAktiv(today: LocalDate): Boolean =
    harAktivStatus() && sluttDato?.isBefore(today) != true

private fun TiltakshistorikkDeltakelse.harAktivStatus(): Boolean = when (this) {
    is TiltakshistorikkDeltakelse.ArenaDeltakelse -> status == ARENA_STATUS_GJENNOMFORES
    is TiltakshistorikkDeltakelse.TeamKometDeltakelse -> status.type == TEAM_KOMET_STATUS_DELTAR
    is TiltakshistorikkDeltakelse.TeamTiltakAvtale -> status == TEAM_TILTAK_STATUS_GJENNOMFORES
}
