package no.nav.syfo.repository

import no.nav.syfo.LocalApplication
import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.dao.VeilederBehandlingDAO
import org.junit.Test
import org.junit.runner.RunWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject

@DirtiesContext
@SpringBootTest(classes = [LocalApplication::class])
@RunWith(SpringRunner::class)
class VeilederBehandlingDAOTest {
    private val AKTOR_ID = "1234567890123"
    private val AKTOR_ID_2 = "2345678901234"
    private val VEILEDER_IDENT = "Z999999"
    private val VEILEDER_IDENT_2 = "Z888888"

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @Inject
    private lateinit var veilederBehandlingDAO: VeilederBehandlingDAO

    @Before
    fun cleanUp() {
        jdbcTemplate.update("DELETE FROM veileder_behandling")
    }

    @Test
    fun sjekkAtKnytningMellomVeilederOgBrukerLagresRiktig() {
        val veilederBrukerKnytning = VeilederBrukerKnytning(VEILEDER_IDENT, AKTOR_ID)

        val lagretId = veilederBehandlingDAO.lagre(veilederBrukerKnytning)

        val veilederBehandlingListe = veilederBehandlingDAO.hentOppgaverPaaVeileder(VEILEDER_IDENT)
        val lagretUUID = veilederBehandlingListe[0].veilederBehandlingUUID
        val lagretAktorId = veilederBehandlingListe[0].aktorId
        val lagretVeilederIdent = veilederBehandlingListe[0].veilederIdent
        val lagretBrukerSistAksessertVerdi = veilederBehandlingListe[0].brukerSistAksessert

        assertThat(lagretId).isGreaterThan(0)
        assertThat(lagretUUID.length).isEqualTo(36)
        assertThat(lagretAktorId).isEqualTo(AKTOR_ID)
        assertThat(lagretVeilederIdent).isEqualTo(VEILEDER_IDENT)
        assertThat(lagretBrukerSistAksessertVerdi).isNull()
    }

    @Test
    fun sjekkAtVeilederBrukerKnytningKanHentes() {
        val veilederBrukerKnytning1 = VeilederBrukerKnytning(VEILEDER_IDENT, AKTOR_ID)
        val veilederBrukerKnytning2 = VeilederBrukerKnytning(VEILEDER_IDENT_2, AKTOR_ID_2)

        veilederBehandlingDAO.lagre(veilederBrukerKnytning1)
        veilederBehandlingDAO.lagre(veilederBrukerKnytning2)

        val aktorIdLagretPaaVeilederIdent = veilederBehandlingDAO.hentOppgaverPaaVeileder(VEILEDER_IDENT)[0].aktorId
        val aktorIdLagretPaaVeilederIdent2 =  veilederBehandlingDAO.hentOppgaverPaaVeileder(VEILEDER_IDENT_2)[0].aktorId

        assertThat(aktorIdLagretPaaVeilederIdent).isEqualTo(AKTOR_ID)
        assertThat(aktorIdLagretPaaVeilederIdent2).isEqualTo(AKTOR_ID_2)
    }

    @Test(expected = DuplicateKeyException::class)
    fun sjekkAtVeilederOgBrukerKunKanAssosieresEnGang() {
        val veilederBrukerKnytning = VeilederBrukerKnytning(VEILEDER_IDENT, AKTOR_ID)

        veilederBehandlingDAO.lagre(veilederBrukerKnytning)
        veilederBehandlingDAO.lagre(veilederBrukerKnytning)

    }

}