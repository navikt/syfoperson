package no.nav.syfo.controller

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.syfo.controller.domain.*
import no.nav.syfo.oidc.OIDCIssuer.AZURE
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.pdl.getName
import no.nav.syfo.service.SkjermingskodeService
import no.nav.syfo.service.VeilederTilgangService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/person"])
@ProtectedWithClaims(issuer = AZURE)
class PersonController @Inject constructor(
        val pdlConsumer: PdlConsumer,
        val skjermingskodeService: SkjermingskodeService,
        val veilederTilgangService: VeilederTilgangService
) {

    @ResponseBody
    @GetMapping(value = ["/navn/{fnr}"], produces = [APPLICATION_JSON_VALUE])
    fun getName(@PathVariable fnr: Fnr): FnrMedNavn {
        return FnrMedNavn(
                fnr.fnr,
                pdlConsumer.person(fnr)?.getName() ?: ""
        )
    }

    @PostMapping(value = ["/info"], produces = [APPLICATION_JSON_VALUE])
    fun getPersoninfoForPersons(@RequestBody brukerFnrListe: List<Fnr>): List<PersonInfo> {
        brukerFnrListe.filter { veilederTilgangService.hasVeilederAccessToPersonWithAzure(it.fnr) }

        return brukerFnrListe.map {
            val person = pdlConsumer.person(it)
            PersonInfo(
                    it.fnr,
                    person?.getName() ?: "",
                    skjermingskodeService.hentBrukersSkjermingskode(person, it.fnr)
            )
        }
    }
}
