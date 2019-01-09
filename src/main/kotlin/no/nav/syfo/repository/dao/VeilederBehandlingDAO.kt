package no.nav.syfo.repository.dao

import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.DbUtil
import no.nav.syfo.repository.DbUtil.tilLocalDateTime
import no.nav.syfo.repository.domain.PVeilederBehandling
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.query

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Repository
@Transactional
class VeilederBehandlingDAO(private val jdbcTemplate: JdbcTemplate, private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {
    val idSekvensnavn = "VEILEDER_BEHANDLING_ID_SEQ"

    fun lagre(veilederBrukerKnytning: VeilederBrukerKnytning): Long {
        val id : Long = DbUtil.nesteSekvensverdi(idSekvensnavn, jdbcTemplate);
        val uuid : String = UUID.randomUUID().toString()

        val lagreSql : String = "INSERT INTO veileder_behandling VALUES(" +
                ":veileder_behandling_id," +
                ":veileder_behandling_uuid," +
                ":aktor_id," +
                ":veileder_ident," +
                ":bruker_sist_aksessert" +
                ")"

        val sqlParametere = mapOf(
                "veileder_behandling_id" to id,
                "veileder_behandling_uuid" to uuid,
                "aktor_id" to veilederBrukerKnytning.aktorId,
                "veileder_ident" to veilederBrukerKnytning.veilederIdent,
                "bruker_sist_aksessert" to null
        )

        namedParameterJdbcTemplate.update(lagreSql, sqlParametere)

        return id
    }

    fun hentOppgaverPaaVeileder(veilederIdent : String) : List<PVeilederBehandling> {
        return jdbcTemplate.query("SELECT * FROM veileder_behandling WHERE veileder_ident = ?", veilederIdent) { rs, i -> PVeilederBehandling(
                rs.getLong("veileder_behandling_id"),
                rs.getString("veileder_behandling_uuid"),
                rs.getString("aktor_id"),
                rs.getString("veileder_ident"),
                tilLocalDateTime(rs.getTimestamp("bruker_sist_aksessert"))) }
    }

    fun slettVeilederBrukerKnytning(veilederBrukerKnytning: VeilederBrukerKnytning): Boolean {
        val antallRaderSlettet = jdbcTemplate.update("DELETE FROM veileder_behandling WHERE aktor_id = ? AND veileder_ident = ?",
                veilederBrukerKnytning.aktorId, veilederBrukerKnytning.veilederIdent)
        return antallRaderSlettet > 0
    }

}
