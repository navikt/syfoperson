package no.nav.syfo.controller

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.service.VeilederBehandlingService
import no.nav.syfo.util.OIDCIssuer.AZURE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/veilederbehandling"])
class VeilederBehandlingController @Inject constructor(val veilederBehandlingService: VeilederBehandlingService) {

    @ResponseBody
    @ProtectedWithClaims(issuer = AZURE)
    @GetMapping(value = ["/veiledere/{veileder}"], produces = [APPLICATION_JSON_VALUE])
    fun hentVeiledersTilknytninger(@PathVariable veileder: String) : List<VeilederBrukerKnytning> {
        return veilederBehandlingService.hentBrukertilknytningerPaaVeileder(veileder)
    }

    @ResponseBody
    @ProtectedWithClaims(issuer = AZURE)
    @GetMapping(value = ["/enheter/{enhet}/veiledere"], produces = [APPLICATION_JSON_VALUE])
    fun hentVeilederTilknytningerPaaEnhet(@PathVariable enhet: String) : List<VeilederBrukerKnytning> {
        return veilederBehandlingService.hentBrukertilknytningerPaaEnhet(enhet)
    }

    @ResponseBody
    @ProtectedWithClaims(issuer = AZURE)
    @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun lagreVeilederTilknytning(@RequestBody veilederBrukerKnytninger: List<VeilederBrukerKnytning>) : List<Long> {
        return veilederBehandlingService.lagreKnytningMellomVeilederOgBruker(veilederBrukerKnytninger)
    }

}
