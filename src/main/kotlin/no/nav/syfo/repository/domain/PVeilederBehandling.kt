package no.nav.syfo.repository.domain

import java.time.LocalDateTime

data class PVeilederBehandling(
        var veilederBehandlingId: Long,
        var veilederBehandlingUUID: String,
        var aktorId: String,
        var veilederIdent: String,
        var brukerSistAksessert: LocalDateTime?,
        var enhet: String,
        var opprettet: LocalDateTime,
        var sistEndret: LocalDateTime
)
