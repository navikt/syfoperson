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
    private val aktorId1 = "aktorId1"
    private val aktorId2 = "aktorId2"
    private val veilederIdent1 = "Z999999"
    private val veilederIdent2 = "Z888888"
    private val veilederIdent3 = "Z777777"
    private val enhet1 = "1234"
    private val enhet2 = "2345"

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
        val veilederBrukerKnytning = VeilederBrukerKnytning(veilederIdent1, aktorId1, enhet1)

        val lagretId = veilederBehandlingDAO.lagre(veilederBrukerKnytning)

        val veilederBehandlingListe = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent1)
        val lagretUUID = veilederBehandlingListe[0].veilederBehandlingUUID
        val lagretAktorId = veilederBehandlingListe[0].aktorId
        val lagretVeilederIdent = veilederBehandlingListe[0].veilederIdent
        val lagretEnhet = veilederBehandlingListe[0].enhet;
        val lagretBrukerSistAksessertVerdi = veilederBehandlingListe[0].brukerSistAksessert

        assertThat(lagretId).isGreaterThan(0)
        assertThat(lagretUUID.length).isEqualTo(36)
        assertThat(lagretAktorId).isEqualTo(aktorId1)
        assertThat(lagretVeilederIdent).isEqualTo(veilederIdent1)
        assertThat(lagretEnhet).isEqualTo(enhet1)
        assertThat(lagretBrukerSistAksessertVerdi).isNull()
    }

    @Test
    fun sjekkAtVeilederBrukerKnytningKanHentes() {
        val veilederBrukerKnytning1 = VeilederBrukerKnytning(veilederIdent1, aktorId1, enhet1)
        val veilederBrukerKnytning2 = VeilederBrukerKnytning(veilederIdent2, aktorId2, enhet1)

        veilederBehandlingDAO.lagre(veilederBrukerKnytning1)
        veilederBehandlingDAO.lagre(veilederBrukerKnytning2)

        val aktorIdLagretPaaVeilederIdent1 = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent1)[0].aktorId
        val aktorIdLagretPaaVeilederIdent2 = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent2)[0].aktorId

        assertThat(aktorIdLagretPaaVeilederIdent1).isEqualTo(aktorId1)
        assertThat(aktorIdLagretPaaVeilederIdent2).isEqualTo(aktorId2)
    }

    @Test
    fun sjekkAtVeilederBrukerKnytningKanHentesPaaEnhet() {
        val veilederBrukerKnytning1 = VeilederBrukerKnytning(veilederIdent1, aktorId1, enhet1)
        val veilederBrukerKnytning2 = VeilederBrukerKnytning(veilederIdent2, aktorId2, enhet1)
        val veilederBrukerKnytning3 = VeilederBrukerKnytning(veilederIdent3, aktorId1, enhet2)

        veilederBehandlingDAO.lagre(veilederBrukerKnytning1)
        veilederBehandlingDAO.lagre(veilederBrukerKnytning2)
        veilederBehandlingDAO.lagre(veilederBrukerKnytning3)

        val aktorIdLagretPaaEnhet1 = veilederBehandlingDAO.hentVeilederBrukerKnytningPaaEnhet(enhet1)[0].aktorId
        val aktorIdLagretPaaEnhet2 = veilederBehandlingDAO.hentVeilederBrukerKnytningPaaEnhet(enhet1)[1].aktorId
        val aktorIdLagretPaaEnhet3 = veilederBehandlingDAO.hentVeilederBrukerKnytningPaaEnhet(enhet2)[0].aktorId

        assertThat(aktorIdLagretPaaEnhet1).isEqualTo(aktorId1)
        assertThat(aktorIdLagretPaaEnhet2).isEqualTo(aktorId2)
        assertThat(aktorIdLagretPaaEnhet3).isEqualTo(aktorId1)
    }

    @Test(expected = DuplicateKeyException::class)
    fun sjekkAtVeilederOgBrukerKunKanAssosieresEnGang() {
        val veilederBrukerKnytning = VeilederBrukerKnytning(veilederIdent1, aktorId1, enhet1)

        veilederBehandlingDAO.lagre(veilederBrukerKnytning)
        veilederBehandlingDAO.lagre(veilederBrukerKnytning)

    }

}
