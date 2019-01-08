package no.nav.syfo.service

import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.dao.VeilederBehandlingDAO
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class VeilederBehandlingService {
    @Inject constructor(veilederBehandlingDAO: VeilederBehandlingDAO) {
        this.veilederBehandlingDAO = veilederBehandlingDAO
    }

    val veilederBehandlingDAO : VeilederBehandlingDAO

    fun hentBrukertilknytningerPaaVeileder(veilederIdent: String): List<VeilederBrukerKnytning> {
        return veilederBehandlingDAO.hentOppgaverPaaVeileder(veilederIdent).map { VeilederBrukerKnytning(it.veilederIdent, it.aktorId) }
    }

    fun lagreKnytningMellomVeilederOgBruker(veilederIdent: String, aktorId: String): Long {
        return veilederBehandlingDAO.lagre(VeilederBrukerKnytning(veilederIdent, aktorId))
    }

}