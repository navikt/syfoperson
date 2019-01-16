package no.nav.syfo.repository

import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.LocalDateTime

object DbUtil {

    fun nesteSekvensverdi(sekvensnavn: String, jdbcTemplate: JdbcTemplate) : Long {
        return jdbcTemplate.queryForObject("select $sekvensnavn.nextval from dual") { rs, _ -> rs.getLong(1) }!!
    }

    fun tilLocalDateTime(timestamp: Timestamp?) : LocalDateTime? {
        return timestamp?.toLocalDateTime()
    }

}
