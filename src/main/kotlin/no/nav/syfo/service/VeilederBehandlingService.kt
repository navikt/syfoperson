package no.nav.syfo.service

import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.dao.VeilederBehandlingDAO
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class VeilederBehandlingService @Inject constructor(val veilederBehandlingDAO: VeilederBehandlingDAO) {

    fun hentBrukertilknytningerPaaVeileder(veilederIdent: String) = veilederBehandlingDAO.hentOppgaverPaaVeileder(veilederIdent).map { VeilederBrukerKnytning(it.veilederIdent, it.aktorId) }

    fun lagreKnytningMellomVeilederOgBruker(veilederBrukerKnytning: VeilederBrukerKnytning) = veilederBehandlingDAO.lagre(veilederBrukerKnytning)

}