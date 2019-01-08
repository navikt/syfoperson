package no.nav.syfo.controller

import no.nav.security.spring.oidc.validation.api.Unprotected
import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.service.VeilederBehandlingService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/veilederbehandling"])
class VeilederBehandlingController {
@Inject constructor(veilederBehandlingService: VeilederBehandlingService) {
    this.veilederBehandlingService = veilederBehandlingService
}

    val veilederBehandlingService: VeilederBehandlingService

    @ResponseBody
    @Unprotected
    @GetMapping(value = ["/veiledere/{veileder}"], produces = [APPLICATION_JSON_VALUE])
    fun hentVeilederTilknytning(@PathVariable veileder : String) : List<VeilederBrukerKnytning> {
        return veilederBehandlingService.hentBrukertilknytningerPaaVeileder(veileder)
    }

    @ResponseBody
    @Unprotected
    @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun lagreVeilederTilknytning(@RequestBody veilederBrukerKnytning: VeilederBrukerKnytning): Long {
        return veilederBehandlingService.lagreKnytningMellomVeilederOgBruker(veilederBrukerKnytning.veilederIdent, veilederBrukerKnytning.aktorId)
    }

}