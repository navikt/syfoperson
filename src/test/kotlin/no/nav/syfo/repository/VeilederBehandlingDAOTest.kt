package no.nav.syfo.repository

import no.nav.syfo.LocalApplication
import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.dao.VeilederBehandlingDAO
import org.junit.Test
import org.junit.runner.RunWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
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
    private val veilederIdent1 = "ident1"
    private val enhet1 = "XXXX"

    private val lagretAktorId1 = "1234567890123"
    private val lagretAktorId2 = "1234567890124"
    private val lagretAktorId3 = "1234567890125"

    private val lagretVeilederIdent1 = "Z999999"
    private val lagretVeilederIdent2 = "Z888888"

    private val lagretEnhet1 = "1234"
    private val lagretEnhet2 = "2345"

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @Inject
    private lateinit var veilederBehandlingDAO: VeilederBehandlingDAO

    @After
    fun cleanUp() {
        jdbcTemplate.update("DELETE FROM veileder_behandling WHERE veileder_behandling_id > 3")
    }

    @Test
    fun sjekkAtKnytningMellomVeilederOgBrukerLagresRiktig() {
        val veilederBrukerKnytning = VeilederBrukerKnytning(veilederIdent1, aktorId1, enhet1)

        val lagretId = veilederBehandlingDAO.lagre(veilederBrukerKnytning)

        val veilederBehandlingListe = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent1)
        val lagretUUID = veilederBehandlingListe[0].veilederBehandlingUUID
        val lagretAktorId = veilederBehandlingListe[0].aktorId
        val lagretVeilederIdent = veilederBehandlingListe[0].veilederIdent
        val lagretEnhet = veilederBehandlingListe[0].enhet
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
        val aktorIdLagretPaaVeilederIdent1 = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(lagretVeilederIdent1)[0].aktorId
        val aktorIdLagretPaaVeilederIdent2 = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(lagretVeilederIdent2)[0].aktorId

        assertThat(aktorIdLagretPaaVeilederIdent1).isEqualTo(lagretAktorId1)
        assertThat(aktorIdLagretPaaVeilederIdent2).isEqualTo(lagretAktorId2)
    }

    @Test
    fun sjekkAtVeilederBrukerKnytningKanHentesPaaEnhet() {
        val aktorId1LagretPaaEnhet1 = veilederBehandlingDAO.hentVeilederBrukerKnytningPaaEnhet(lagretEnhet1)[0].aktorId
        val aktorId2LagretPaaEnhet1 = veilederBehandlingDAO.hentVeilederBrukerKnytningPaaEnhet(lagretEnhet1)[1].aktorId
        val aktorId3LagretPaaEnhet2 = veilederBehandlingDAO.hentVeilederBrukerKnytningPaaEnhet(lagretEnhet2)[0].aktorId

        assertThat(aktorId1LagretPaaEnhet1).isEqualTo(lagretAktorId1)
        assertThat(aktorId2LagretPaaEnhet1).isEqualTo(lagretAktorId2)
        assertThat(aktorId3LagretPaaEnhet2).isEqualTo(lagretAktorId3)
    }

    @Test(expected = DuplicateKeyException::class)
    fun sjekkAtVeilederOgBrukerKunKanAssosieresEnGang() {
        val veilederBrukerKnytning = VeilederBrukerKnytning(lagretVeilederIdent1, lagretAktorId1, lagretEnhet1)

        veilederBehandlingDAO.lagre(veilederBrukerKnytning)
    }

}
