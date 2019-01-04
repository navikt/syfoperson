package no.nav.syfo.repository.dao

import no.nav.syfo.repository.DbUtil
import no.nav.syfo.repository.domain.PVeilederBehandling
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
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

    fun lagre(pVeilederBehandling: PVeilederBehandling): Long {
        val id : Long = DbUtil.nesteSekvensverdi(idSekvensnavn, jdbcTemplate);
        val uuid : String = UUID.randomUUID().toString()

        val lagreSql : String = "INSERT INTO veileder_behandling VALUES(" +
                ":veileder_behandling_id," +
                ":veileder_behandling_uuid," +
                ":aktor_id," +
                ":veileder_ident," +
                ":under_behandling" +
                ")"

        val sqlParametere : MapSqlParameterSource = MapSqlParameterSource()
                .addValue("veileder_behandling_id", id)
                .addValue("veileder_behandling_uuid", uuid)
                .addValue("aktor_id", pVeilederBehandling.aktorId)
                .addValue("veileder_ident", pVeilederBehandling.veilederIdent)
                .addValue("under_behandling", pVeilederBehandling.underBehandling)

        namedParameterJdbcTemplate.update(lagreSql, sqlParametere)

        return id
    }

    fun hentOppgaverPaaVeileder(veilederIdent : String) : List<PVeilederBehandling> {
        return jdbcTemplate.query("SELECT * FROM veileder_behandling WHERE veileder_ident = ?", veilederIdent) { rs, i -> PVeilederBehandling(
                rs.getLong("veileder_behandling_id"),
                rs.getString("veileder_behandling_uuid"),
                rs.getString("aktor_id"),
                rs.getString("veileder_ident"),
                rs.getBoolean("under_behandling")) }
    }

}
