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
    @PostMapping(value = ["veiledere/{veileder}/{aktorId}"], consumes = [APPLICATION_JSON_VALUE])
    fun lagreVeilederTilknytning(@PathVariable veileder: String, @PathVariable aktorId: String) {
        veilederBehandlingService.lagreKnytningMellomVeilederOgBruker(veileder, aktorId)
    }

    /*
    @ResponseBody
    @Unprotected
    @PostMapping(consumes = [APPLICATION_JSON_VALUE])
    fun lagreVeilederTilknytning(veilederBrukerKnytning: VeilederBrukerKnytning) {

    }
    */

}