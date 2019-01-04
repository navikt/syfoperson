package no.nav.syfo.repository

import org.springframework.jdbc.core.JdbcTemplate

object DbUtil {

    fun nesteSekvensverdi(sekvensnavn: String, jdbcTemplate: JdbcTemplate): Long {
        return jdbcTemplate.queryForObject("select $sekvensnavn.nextval from dual") { rs, rowNum -> rs.getLong(1) }!!
    }

}
