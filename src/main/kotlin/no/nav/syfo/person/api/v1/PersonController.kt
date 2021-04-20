package no.nav.syfo.person.api.v1

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.consumer.dkif.DigitalKontaktinfoBolk
import no.nav.syfo.consumer.dkif.DkifConsumer
import no.nav.syfo.consumer.pdl.*
import no.nav.syfo.consumer.skjermedepersoner.SkjermedePersonerPipConsumer
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import no.nav.syfo.person.api.domain.*
import no.nav.syfo.person.skjermingskode.SkjermingskodeService
import no.nav.syfo.util.getPersonIdent
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/person"])
@ProtectedWithClaims(issuer = AZURE)
class PersonController @Inject constructor(
    val dkifConsumer: DkifConsumer,
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
    @GetMapping(value = ["/navn"], produces = [APPLICATION_JSON_VALUE])
    fun getName(
        @RequestHeader headers: MultiValueMap<String, String>
    ): FnrMedNavn {
        val requestedPersonIdent = headers.getPersonIdent()?.let { personIdent ->
            Fnr(personIdent)
        } ?: throw IllegalArgumentException("Did not find a PersonIdent in request headers")

        return FnrMedNavn(
            requestedPersonIdent.fnr,
            pdlConsumer.person(requestedPersonIdent)?.getName() ?: ""
        )
    }

    @ResponseBody
    @GetMapping(value = ["/egenansatt"], produces = [APPLICATION_JSON_VALUE])
    fun isEgenAnsatt(
        @RequestHeader headers: MultiValueMap<String, String>
    ): Boolean {
        val requestedPersonIdent = headers.getPersonIdent()?.let { personIdent ->
            Fnr(personIdent)
        } ?: throw IllegalArgumentException("Did not find a PersonIdent in request headers")

        return skjermedePersonerPipConsumer.erSkjermet(requestedPersonIdent.fnr)
    }

    @ResponseBody
    @GetMapping(value = ["/diskresjonskode"], produces = [APPLICATION_JSON_VALUE])
    fun getDiskresjonskode(
        @RequestHeader headers: MultiValueMap<String, String>
    ): String {
        val requestedPersonIdent = headers.getPersonIdent()?.let { personIdent ->
            Fnr(personIdent)
        } ?: throw IllegalArgumentException("Did not find a PersonIdent in request headers")

        return pdlConsumer.person(requestedPersonIdent)?.getDiskresjonskode() ?: ""
    }

    @ResponseBody
    @GetMapping(value = ["/adressebeskyttelse"], produces = [APPLICATION_JSON_VALUE])
    fun getAdressebeskyttelse(
        @RequestHeader headers: MultiValueMap<String, String>
    ): AdressebeskyttelseResponse {
        val requestedPersonIdent = headers.getPersonIdent()?.let { personIdent ->
            Fnr(personIdent)
        } ?: throw IllegalArgumentException("Did not find a PersonIdent in request headers")

        veilederTilgangConsumer.throwExceptionIfDeniedAccess(requestedPersonIdent)

        val adressebeskyttelse = pdlConsumer.isKode6Or7(requestedPersonIdent)
        return AdressebeskyttelseResponse(
            beskyttet = adressebeskyttelse
        )
    }

    @ResponseBody
    @GetMapping(value = ["/adresse"], produces = [APPLICATION_JSON_VALUE])
    fun getAdresse(
        @RequestHeader headers: MultiValueMap<String, String>
    ): PersonAdresseResponse {
        val requestedPersonIdent = headers.getPersonIdent()?.let { personIdent ->
            Fnr(personIdent)
        } ?: throw IllegalArgumentException("Did not find a PersonIdent in request headers")

        veilederTilgangConsumer.throwExceptionIfDeniedAccess(requestedPersonIdent)

        val person = pdlConsumer.person(requestedPersonIdent)
        return PersonAdresseResponse(
            navn = person?.getName() ?: "",
            bostedsadresse = person?.bostedsadresse(),
            kontaktadresse = person?.kontaktadresse(),
            oppholdsadresse = person?.oppholdsadresse()
        )
    }

    @ResponseBody
    @GetMapping(value = ["/kontaktinformasjon"], produces = [APPLICATION_JSON_VALUE])
    fun getKontaktInfo(
        @RequestHeader headers: MultiValueMap<String, String>
    ): DigitalKontaktinfoBolk {
        val requestedPersonIdent = headers.getPersonIdent()?.let { personIdent ->
            Fnr(personIdent)
        } ?: throw IllegalArgumentException("Did not find a PersonIdent in request headers")

        veilederTilgangConsumer.throwExceptionIfDeniedAccess(requestedPersonIdent)

        return dkifConsumer.digitalKontaktinfoBolk(requestedPersonIdent)
    }
}
