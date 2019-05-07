package no.nav.syfo.service

import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.dao.VeilederBehandlingDAO
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class VeilederBehandlingService @Inject constructor(val veilederBehandlingDAO: VeilederBehandlingDAO) {

    fun hentBrukertilknytningerPaVeileder(veilederIdent: String) = veilederBehandlingDAO.hentBrukereTilknyttetVeileder(veilederIdent).map { VeilederBrukerKnytning(it.veilederIdent, it.aktorId, it.enhet) }

    fun hentBrukertilknytningerPaEnhet(enhetId: String) = veilederBehandlingDAO.hentVeilederBrukerKnytningPaEnhet(enhetId).map { VeilederBrukerKnytning(it.veilederIdent, it.aktorId, it.enhet) }

    fun lagreKnytningMellomVeilederOgBruker(veilederBrukerKnytninger: List<VeilederBrukerKnytning>) = veilederBrukerKnytninger.map { veilederBehandlingDAO.lagre(it) }

}
