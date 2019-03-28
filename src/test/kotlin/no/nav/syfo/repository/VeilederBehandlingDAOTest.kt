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
    private val aktorId = "1234567890123"
    private val aktorId2 = "2345678901234"
    private val veilederIdent = "Z999999"
    private val veilederIdent2 = "Z888888"

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
        val veilederBrukerKnytning = VeilederBrukerKnytning(veilederIdent, aktorId)

        val lagretId = veilederBehandlingDAO.lagre(veilederBrukerKnytning)

        val veilederBehandlingListe = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent)
        val lagretUUID = veilederBehandlingListe[0].veilederBehandlingUUID
        val lagretAktorId = veilederBehandlingListe[0].aktorId
        val lagretVeilederIdent = veilederBehandlingListe[0].veilederIdent
        val lagretBrukerSistAksessertVerdi = veilederBehandlingListe[0].brukerSistAksessert

        assertThat(lagretId).isGreaterThan(0)
        assertThat(lagretUUID.length).isEqualTo(36)
        assertThat(lagretAktorId).isEqualTo(aktorId)
        assertThat(lagretVeilederIdent).isEqualTo(veilederIdent)
        assertThat(lagretBrukerSistAksessertVerdi).isNull()
    }

    @Test
    fun sjekkAtVeilederBrukerKnytningKanHentes() {
        val veilederBrukerKnytning1 = VeilederBrukerKnytning(veilederIdent, aktorId)
        val veilederBrukerKnytning2 = VeilederBrukerKnytning(veilederIdent2, aktorId2)

        veilederBehandlingDAO.lagre(veilederBrukerKnytning1)
        veilederBehandlingDAO.lagre(veilederBrukerKnytning2)

        val aktorIdLagretPaaVeilederIdent = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent)[0].aktorId
        val aktorIdLagretPaaVeilederIdent2 =  veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent2)[0].aktorId

        assertThat(aktorIdLagretPaaVeilederIdent).isEqualTo(aktorId)
        assertThat(aktorIdLagretPaaVeilederIdent2).isEqualTo(aktorId2)
    }

    @Test(expected = DuplicateKeyException::class)
    fun sjekkAtVeilederOgBrukerKunKanAssosieresEnGang() {
        val veilederBrukerKnytning = VeilederBrukerKnytning(veilederIdent, aktorId)

        veilederBehandlingDAO.lagre(veilederBrukerKnytning)
        veilederBehandlingDAO.lagre(veilederBrukerKnytning)

    }

}
