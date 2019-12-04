package no.nav.syfo.controller

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.syfo.controller.domain.*
import no.nav.syfo.oidc.OIDCIssuer.AZURE
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.service.*
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/person"])
@ProtectedWithClaims(issuer = AZURE)
class PersonController @Inject constructor(
        val pdlConsumer: PdlConsumer,
        val personService: PersonService,
        val skjermingskodeService: SkjermingskodeService,
        val veilederTilgangService: VeilederTilgangService
) {

    @ResponseBody
    @GetMapping(value = ["/navn/{fnr}"], produces = [APPLICATION_JSON_VALUE])
    fun getName(@PathVariable fnr: Fnr): FnrMedNavn {
        return FnrMedNavn(
                fnr.fnr,
                pdlConsumer.personName(fnr) ?: ""
        )
    }

    @ResponseBody
    @PostMapping(value = ["/navn"], produces = [APPLICATION_JSON_VALUE])
    fun getNavnForPerson(@RequestBody brukerFnrListe: List<Fnr>): List<FnrMedNavn> {
        return brukerFnrListe.map { FnrMedNavn(it.fnr, personService.hentNavnFraPerson(personService.hentPersonFraFnr(it.fnr))) }
    }

    @PostMapping(value = ["/info"], produces = [APPLICATION_JSON_VALUE])
    fun getPersoninfoForPersons(@RequestBody brukerFnrListe: List<Fnr>): List<PersonInfo> {
        brukerFnrListe.filter { veilederTilgangService.hasVeilederAccessToPersonWithAzure(it.fnr) }

        return brukerFnrListe.map {
            val person = personService.hentPersonFraFnr(it.fnr)
            PersonInfo(
                    it.fnr,
                    pdlConsumer.personName(it) ?: "",
                    skjermingskodeService.hentBrukersSkjermingskode(person, it.fnr)
            )
        }
    }
}
