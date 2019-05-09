package no.nav.syfo.controller

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.syfo.controller.domain.Fnr
import no.nav.syfo.controller.domain.FnrMedNavn
import no.nav.syfo.service.PersonService
import no.nav.syfo.util.OIDCIssuer.AZURE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/person"])
@ProtectedWithClaims(issuer = AZURE)
class PersonController @Inject constructor(val personService: PersonService) {

    @ResponseBody
    @PostMapping(value = ["/navn"], produces = [APPLICATION_JSON_VALUE])
    fun hentNavnPaBrukere(@RequestBody brukerFnrListe: List<Fnr>) : List<FnrMedNavn> {
        return brukerFnrListe.map { FnrMedNavn(it.fnr, personService.hentNavnFraFnr(it.fnr)) }
    }

}
