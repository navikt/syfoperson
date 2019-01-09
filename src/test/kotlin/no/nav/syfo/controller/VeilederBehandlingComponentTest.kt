package no.nav.syfo.controller

import no.nav.syfo.LocalApplication
import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.dao.VeilederBehandlingDAO
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class VeilederBehandlingComponentTest {

    @Inject
    private lateinit var veilederBehandlingController : VeilederBehandlingController

    @Inject
    private lateinit var veilederBehandlingDAO : VeilederBehandlingDAO

    private val brukerAktorId1 = "1234567890123"
    private val brukerAktorId2 = "2345678901234"
    private val brukerAktorId3 = "3456789012345"

    private val veilederIdent1 = "Z888888"
    private val veilederIdent2 = "Z999999"

    private val tilknytning1 = VeilederBrukerKnytning(veilederIdent1, brukerAktorId1)
    private val tilknytning2 = VeilederBrukerKnytning(veilederIdent1, brukerAktorId2)
    private val tilknytning3 = VeilederBrukerKnytning(veilederIdent2, brukerAktorId3)

    @After
    fun cleanUp() {
        fjernBrukere()
    }

    @Test
    fun sjekkAtVeilederBrukerTilknytningerKanLagresOgHentesRiktig() {
        veilederBehandlingController.lagreVeilederTilknytning(tilknytning1)
        veilederBehandlingController.lagreVeilederTilknytning(tilknytning2)
        veilederBehandlingController.lagreVeilederTilknytning(tilknytning3)

        val knytningerPaaVeileder1 = veilederBehandlingController.hentVeiledersTilknytninger(veilederIdent1)
        val knytningerPaaVeileder2 = veilederBehandlingController.hentVeiledersTilknytninger(veilederIdent2)

        assertThat(knytningerPaaVeileder1).contains(tilknytning1)
        assertThat(knytningerPaaVeileder1).contains(tilknytning2)
        assertThat(knytningerPaaVeileder2).contains(tilknytning3)

    }


    fun fjernBrukere() {
        arrayOf(tilknytning1, tilknytning2, tilknytning3).forEach { veilederBehandlingDAO.slettVeilederBrukerKnytning(it) }
    }

}
