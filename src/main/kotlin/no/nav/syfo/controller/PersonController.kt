package no.nav.syfo.controller

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.syfo.controller.domain.Fnr
import no.nav.syfo.service.PersonService
import no.nav.syfo.util.OIDCIssuer.AZURE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/person"])
class PersonController @Inject constructor(val personService: PersonService) {

    @ResponseBody
    @ProtectedWithClaims(issuer = AZURE)
    @PostMapping(value = ["/navn"], produces = [APPLICATION_JSON_VALUE])
    fun hentNavnPaaBrukere(@RequestBody brukerFnrListe: List<Fnr>) : List<String> {
        return brukerFnrListe.map { personService.hentNavnFraFnr(it.fnr) }
    }

}