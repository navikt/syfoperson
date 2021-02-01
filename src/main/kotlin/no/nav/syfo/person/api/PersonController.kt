package no.nav.syfo.person.api

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.consumer.pdl.*
import no.nav.syfo.consumer.skjermedepersoner.SkjermedePersonerPipConsumer
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import no.nav.syfo.person.api.domain.*
import no.nav.syfo.person.skjermingskode.SkjermingskodeService
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/person"])
@ProtectedWithClaims(issuer = AZURE)
class PersonController @Inject constructor(
    val pdlConsumer: PdlConsumer,
    val skjermingskodeService: SkjermingskodeService,
    val skjermedePersonerPipConsumer: SkjermedePersonerPipConsumer,
    val veilederTilgangConsumer: VeilederTilgangConsumer
) {
    @PostMapping(value = ["/info"], produces = [APPLICATION_JSON_VALUE])
    fun getPersoninfoForPersons(@RequestBody brukerFnrListe: List<Fnr>): List<PersonInfo> {
        brukerFnrListe.filter { veilederTilgangConsumer.hasVeilederAccessToPersonWithAzure(it.fnr) }

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

    @ResponseBody
    @GetMapping(value = ["/adressebeskyttelse"], produces = [APPLICATION_JSON_VALUE])
    fun getAdressebeskyttelse(
        @RequestHeader headers: MultiValueMap<String, String>
    ): AdressebeskyttelseResponse {
        val requestedPersonIdent = headers.getFirst(NAV_PERSONIDENT_HEADER.toLowerCase())
        if (requestedPersonIdent.isNullOrEmpty()) {
            throw IllegalArgumentException("Did not find a PersonIdent in request headers")
        } else {
            val fodselsnummer = Fnr(requestedPersonIdent)

            veilederTilgangConsumer.throwExceptionIfDeniedAccess(fodselsnummer)

            val adressebeskyttelse = pdlConsumer.isKode6Or7(fodselsnummer)
            return AdressebeskyttelseResponse(
                beskyttet = adressebeskyttelse
            )
        }
    }

    @ResponseBody
    @GetMapping(value = ["/adresse"], produces = [APPLICATION_JSON_VALUE])
    fun getAdresse(
        @RequestHeader headers: MultiValueMap<String, String>
    ): PersonAdresseReponse {
        val requestedPersonIdent: String? = headers.getFirst(NAV_PERSONIDENT_HEADER.toLowerCase())
        if (requestedPersonIdent.isNullOrEmpty()) {
            throw IllegalArgumentException("Did not find a PersonIdent in request headers")
        }
        val fodselsnummer = Fnr(requestedPersonIdent)

        veilederTilgangConsumer.throwExceptionIfDeniedAccess(fodselsnummer)

        val person = pdlConsumer.person(fodselsnummer)
        return PersonAdresseReponse(
            navn = person?.getName() ?: "",
            bostedsadresse = person?.bostedsadresse(),
            kontaktadresse = person?.kontaktadresse(),
            oppholdsadresse = person?.oppholdsadresse()
        )
    }
}
