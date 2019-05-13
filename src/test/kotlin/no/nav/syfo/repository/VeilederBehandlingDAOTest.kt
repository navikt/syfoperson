package no.nav.syfo.repository

import no.nav.syfo.LocalApplication
import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.dao.VeilederBehandlingDAO
import org.junit.Test
import org.junit.runner.RunWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject

@DirtiesContext
@SpringBootTest(classes = [LocalApplication::class])
@RunWith(SpringRunner::class)
class VeilederBehandlingDAOTest {
    private val fnr1 = "fnr1"
    private val fnr2 = "fnr2"
    private val veilederIdent1 = "ident1"
    private val veilederIdent2 = "ident2"
    private val enhet1 = "XXXX"
    private val enhet2 = "YYYY"
    private val enhet3 = "ZZZZ"

    private val lagretFnr1 = "12345678901"
    private val lagretFnr2 = "12345678902"
    private val lagretFnr3 = "12345678903"

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
    fun `Sjekk at VeilederBrukerKnytning lagres riktig`() {
        val veilederBrukerKnytning = VeilederBrukerKnytning(veilederIdent1, fnr1, enhet1)

        val lagretId = veilederBehandlingDAO.lagre(veilederBrukerKnytning)

        val veilederBehandlingListe = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent1)
        val lagretUUID = veilederBehandlingListe[0].veilederBehandlingUUID
        val lagretFnr = veilederBehandlingListe[0].fnr
        val lagretVeilederIdent = veilederBehandlingListe[0].veilederIdent
        val lagretEnhet = veilederBehandlingListe[0].enhet
        val lagretBrukerSistAksessertVerdi = veilederBehandlingListe[0].brukerSistAksessert
        val lagretOpprettetVerdi = veilederBehandlingListe[0].opprettet
        val lagretSistEndretVerdi = veilederBehandlingListe[0].sistEndret

        assertThat(lagretId).isGreaterThan(0)
        assertThat(lagretUUID.length).isEqualTo(36)
        assertThat(lagretFnr).isEqualTo(fnr1)
        assertThat(lagretVeilederIdent).isEqualTo(veilederIdent1)
        assertThat(lagretEnhet).isEqualTo(enhet1)
        assertThat(lagretBrukerSistAksessertVerdi).isNull()
        assertThat(lagretOpprettetVerdi).isNotNull()
        assertThat(lagretSistEndretVerdi).isNotNull()
    }

    @Test
    fun `Sjekk at veilederBrukerKnytning kan hentes`() {
        val fnrLagretPaVeilederIdent1 = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(lagretVeilederIdent1)[0].fnr
        val fnrLagretPaVeilederIdent2 = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(lagretVeilederIdent2)[0].fnr

        assertThat(fnrLagretPaVeilederIdent1).isEqualTo(lagretFnr1)
        assertThat(fnrLagretPaVeilederIdent2).isEqualTo(lagretFnr2)
    }

    @Test
    fun `Sjekk at veilederBrukerKnytning kan hentes pa enhet`() {
        val fnr1LagretPaEnhet1 = veilederBehandlingDAO.hentVeilederBrukerKnytningPaEnhet(lagretEnhet1)[0].fnr
        val fnr2LagretPaEnhet1 = veilederBehandlingDAO.hentVeilederBrukerKnytningPaEnhet(lagretEnhet1)[1].fnr
        val fnr3LagretPaEnhet2 = veilederBehandlingDAO.hentVeilederBrukerKnytningPaEnhet(lagretEnhet2)[0].fnr

        assertThat(fnr1LagretPaEnhet1).isEqualTo(lagretFnr1)
        assertThat(fnr2LagretPaEnhet1).isEqualTo(lagretFnr2)
        assertThat(fnr3LagretPaEnhet2).isEqualTo(lagretFnr3)
    }

    @Test
    fun `Sjekk at veilederBrukerKnytning kan oppdatere tildelt enhet`() {
        val veilederBrukerKnytning1 = VeilederBrukerKnytning(veilederIdent2, fnr2, enhet2)
        val veilederBrukerKnytning2 = VeilederBrukerKnytning(veilederIdent2, fnr2, enhet3)

        val lagretId1 = veilederBehandlingDAO.lagre(veilederBrukerKnytning1)

        val lagretId2 = veilederBehandlingDAO.lagre(veilederBrukerKnytning2)

        assertThat(lagretId1).isEqualTo(lagretId2)

        val knytningerPaVeileder = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent2)

        val fnrPaKnytning = knytningerPaVeileder[0].fnr
        val veilederIdentPaKnytning = knytningerPaVeileder[0].veilederIdent
        val enhetPaKnytning = knytningerPaVeileder[0].enhet

        assertThat(fnrPaKnytning).isEqualTo(veilederBrukerKnytning2.fnr)
        assertThat(veilederIdentPaKnytning).isEqualTo(veilederBrukerKnytning2.veilederIdent)
        assertThat(enhetPaKnytning).isEqualTo(veilederBrukerKnytning2.enhet)
    }

}
