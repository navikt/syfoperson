package no.nav.syfo.repository.domain

data class PVeilederBehandling(
        var veilederBehandlingId : Long,
        var veilederBehandlingUUID : String,
        var aktorId : String,
        var veilederIdent : String,
        var ferdigBehandlet : Boolean
)