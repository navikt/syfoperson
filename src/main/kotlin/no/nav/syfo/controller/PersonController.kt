package no.nav.syfo.controller

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.syfo.controller.domain.*
import no.nav.syfo.service.*
import no.nav.syfo.util.OIDCIssuer.AZURE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/person"])
@ProtectedWithClaims(issuer = AZURE)
class PersonController @Inject constructor(
        val personService: PersonService,
        val skjermingskodeService: SkjermingskodeService,
        val veilederTilgangService: VeilederTilgangService
) {

    @ResponseBody
    @PostMapping(value = ["/navn"], produces = [APPLICATION_JSON_VALUE])
    fun hentNavnPaBrukere(@RequestBody brukerFnrListe: List<Fnr>): List<FnrMedNavn> {
        return brukerFnrListe.map { FnrMedNavn(it.fnr, personService.hentNavnFraPerson(personService.hentPersonFraFnr(it.fnr))) }
    }

    @PostMapping(value = ["/info"], produces = [APPLICATION_JSON_VALUE])
    fun hentPersoninfoBrukere(@RequestBody brukerFnrListe: List<Fnr>): List<PersonInfo> {
        brukerFnrListe.filter { veilederTilgangService.sjekkVeiledersTilgangTilPersonViaAzure(it.fnr) }

        return brukerFnrListe.map {
            val person = personService.hentPersonFraFnr(it.fnr)
            PersonInfo(
                    it.fnr,
                    personService.hentNavnFraPerson(person),
                    skjermingskodeService.hentBrukersSkjermingskode(person, it.fnr)
            )
        }
    }
}
