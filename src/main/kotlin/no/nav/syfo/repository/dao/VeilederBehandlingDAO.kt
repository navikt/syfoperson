package no.nav.syfo.repository.dao

import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.DbUtil
import no.nav.syfo.repository.DbUtil.tilLocalDateTime
import no.nav.syfo.repository.DbUtil.tilLocalDateTimeNullable
import no.nav.syfo.repository.domain.PVeilederBehandling
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.query

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime.now
import java.util.*

@Service
@Repository
@Transactional
class VeilederBehandlingDAO(private val jdbcTemplate: JdbcTemplate, private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {
    val idSekvensnavn = "VEILEDER_BEHANDLING_ID_SEQ"

    fun lagre(veilederBrukerKnytning: VeilederBrukerKnytning) : Long {
        val id = DbUtil.nesteSekvensverdi(idSekvensnavn, jdbcTemplate)
        val uuid = UUID.randomUUID().toString()
        val tidspunkt = Timestamp.valueOf(now())

        val lagreSql = "INSERT INTO veileder_behandling VALUES(" +
                ":veileder_behandling_id," +
                ":veileder_behandling_uuid," +
                ":aktor_id," +
                ":veileder_ident," +
                ":bruker_sist_aksessert," +
                ":enhet," +
                ":opprettet," +
                ":sist_endret" +
                ")"

        val sqlParametere = mapOf(
                "veileder_behandling_id" to id,
                "veileder_behandling_uuid" to uuid,
                "aktor_id" to veilederBrukerKnytning.aktorId,
                "veileder_ident" to veilederBrukerKnytning.veilederIdent,
                "bruker_sist_aksessert" to null,
                "enhet" to veilederBrukerKnytning.enhet,
                "opprettet" to tidspunkt,
                "sist_endret" to tidspunkt
        )

        try {
            namedParameterJdbcTemplate.update(lagreSql, sqlParametere)
        } catch (e: DuplicateKeyException) {
            return oppdaterEnhetPaaTilknytning(veilederBrukerKnytning)
        }
        return id
    }

    fun oppdaterEnhetPaaTilknytning(veilederBrukerKnytning: VeilederBrukerKnytning) : Long {
        val id = jdbcTemplate.query("SELECT VEILEDER_BEHANDLING_ID FROM veileder_behandling WHERE aktor_id = ? AND veileder_ident = ?", veilederBrukerKnytning.aktorId, veilederBrukerKnytning.veilederIdent) { rs, _ ->
                rs.getLong("veileder_behandling_id")
        }[0]
        jdbcTemplate.update("UPDATE VEILEDER_BEHANDLING SET ENHET = ? WHERE VEILEDER_BEHANDLING_ID = ?", veilederBrukerKnytning.enhet, id)
        return id
    }

    fun hentBrukereTilknyttetVeileder(veilederIdent: String) : List<PVeilederBehandling> {
        return jdbcTemplate.query("SELECT * FROM veileder_behandling WHERE veileder_ident = ?", VeilederBehandlingRowMapper(), veilederIdent)
    }

    fun hentVeilederBrukerKnytningPaaEnhet(enhetId: String) : List<PVeilederBehandling> {
        return jdbcTemplate.query("SELECT * FROM veileder_behandling WHERE enhet = ?", VeilederBehandlingRowMapper(), enhetId)
    }

    fun slettVeilederBrukerKnytning(veilederBrukerKnytning: VeilederBrukerKnytning) : Boolean {
        val antallRaderSlettet = jdbcTemplate.update("DELETE FROM veileder_behandling WHERE aktor_id = ? AND veileder_ident = ?",
                veilederBrukerKnytning.aktorId, veilederBrukerKnytning.veilederIdent)
        return antallRaderSlettet > 0
    }

    private inner class VeilederBehandlingRowMapper : RowMapper<PVeilederBehandling> {
        override fun mapRow(rs: ResultSet, rowNum: Int): PVeilederBehandling? {
            return PVeilederBehandling(
                    rs.getLong("veileder_behandling_id"),
                    rs.getString("veileder_behandling_uuid"),
                    rs.getString("aktor_id"),
                    rs.getString("veileder_ident"),
                    tilLocalDateTimeNullable(rs.getTimestamp("bruker_sist_aksessert")),
                    rs.getString("enhet"),
                    tilLocalDateTime(rs.getTimestamp("opprettet")),
                    tilLocalDateTime(rs.getTimestamp("sist_endret"))
            )
        }

    }

}
