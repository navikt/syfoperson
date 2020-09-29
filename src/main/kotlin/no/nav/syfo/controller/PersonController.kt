package no.nav.syfo.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.controller.domain.*
import no.nav.syfo.oidc.OIDCIssuer.AZURE
import no.nav.syfo.pdl.*
import no.nav.syfo.service.SkjermingskodeService
import no.nav.syfo.service.VeilederTilgangService
import no.nav.syfo.skjermedepersoner.SkjermedePersonerPipConsumer
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/person"])
@ProtectedWithClaims(issuer = AZURE)
class PersonController @Inject constructor(
    val pdlConsumer: PdlConsumer,
    val skjermingskodeService: SkjermingskodeService,
    val skjermedePersonerPipConsumer: SkjermedePersonerPipConsumer,
    val veilederTilgangService: VeilederTilgangService
) {
    @PostMapping(value = ["/info"], produces = [APPLICATION_JSON_VALUE])
    fun getPersoninfoForPersons(@RequestBody brukerFnrListe: List<Fnr>): List<PersonInfo> {
        brukerFnrListe.filter { veilederTilgangService.hasVeilederAccessToPersonWithAzure(it.fnr) }

        return brukerFnrListe.map {
            val person = pdlConsumer.person(it)
            PersonInfo(
                it.fnr,
                skjermingskodeService.hentBrukersSkjermingskode(person, it.fnr)
            )
        }
    }

    @ResponseBody
    @GetMapping(value = ["/navn/{fnr}"], produces = [APPLICATION_JSON_VALUE])
    fun getName(@PathVariable fnr: Fnr): FnrMedNavn {
        return FnrMedNavn(
            fnr.fnr,
            pdlConsumer.person(fnr)?.getName() ?: ""
        )
    }

    @ResponseBody
    @GetMapping(value = ["/egenansatt/{fnr}"], produces = [APPLICATION_JSON_VALUE])
    fun isEgenAnsatt(@PathVariable fnr: Fnr): Boolean {
        return skjermedePersonerPipConsumer.erSkjermet(fnr.fnr)
    }

    @ResponseBody
    @GetMapping(value = ["/diskresjonskode/{fnr}"], produces = [APPLICATION_JSON_VALUE])
    fun getDiskresjonskode(@PathVariable fnr: Fnr): String {
        return pdlConsumer.person(fnr)?.getDiskresjonskode() ?: ""
    }
}
