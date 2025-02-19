package no.nav.syfo.person.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.client.kodeverk.KodeverkClient
import no.nav.syfo.client.krr.KRRClient
import no.nav.syfo.client.krr.toSyfomodiapersonKontaktinfo
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.person.api.domain.*
import no.nav.syfo.person.api.domain.syfomodiaperson.SyfomodiapersonBrukerinfo
import no.nav.syfo.person.skjermingskode.SkjermingskodeService
import no.nav.syfo.util.*

const val apiPersonBasePath = "/syfoperson/api/v2/person"

const val apiPersonAdressePath = "/adresse"
const val apiPersonDiskresjonskodePath = "/diskresjonskode"
const val apiPersonEgenansattPath = "/egenansatt"
const val apiPersonInfoPath = "/info"
const val apiPersonKontaktinformasjonPath = "/kontaktinformasjon"
const val apiPersonNavnPath = "/navn"

const val apiPersonBrukerinfoPath = "/brukerinfo"

fun Route.registrerPersonApi(
    krrClient: KRRClient,
    pdlClient: PdlClient,
    skjermingskodeService: SkjermingskodeService,
    skjermedePersonerPipClient: SkjermedePersonerPipClient,
    veilederTilgangskontrollClient: VeilederTilgangskontrollClient,
    kodeverkClient: KodeverkClient,
) {
    route(apiPersonBasePath) {
        post(apiPersonInfoPath) {
            try {
                val personidenterRequestBody = call.receive<List<PersonInfoRequest>>()
                val personidenter = personidenterRequestBody.map { PersonIdentNumber(it.fnr) }

                val callId = getCallId()
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val grantedAccessList = veilederTilgangskontrollClient.hasVeilederAccessToPersonList(
                    callId = callId,
                    personidenter = personidenter,
                    token = token,
                )
                val response: List<PersonInfo> = skjermingskodeService.hentSkjermingskodeForPersonidenter(
                    callId = callId,
                    personidenter = grantedAccessList,
                    token = token,
                )
                call.respond(response)
            } catch (ex: Exception) {
                handleApiError(
                    ex = ex,
                    resource = apiPersonInfoPath,
                )
            }
        }

        get(apiPersonNavnPath) {
            personRequestHandler(
                resource = apiPersonNavnPath,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            ) {
                val personIdentNumber = getPersonIdent()?.let { requestedPersonIdent ->
                    PersonIdentNumber(requestedPersonIdent)
                } ?: throw IllegalArgumentException("No personIdentNumber supplied in header")

                val callId = getCallId()

                val navn = pdlClient.person(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                )?.hentPerson?.fullName ?: ""

                val response = FnrMedNavn(
                    fnr = personIdentNumber.value,
                    navn = navn,
                )
                call.respond(response)
            }
        }

        get(apiPersonEgenansattPath) {
            personRequestHandler(
                resource = apiPersonEgenansattPath,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            ) {
                val personIdentNumber = getPersonIdent()?.let { requestedPersonIdent ->
                    PersonIdentNumber(requestedPersonIdent)
                } ?: throw IllegalArgumentException("No personIdentNumber supplied in header")

                val callId = getCallId()
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val isSkjermet = skjermedePersonerPipClient.isSkjermet(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                    oboToken = skjermedePersonerPipClient.getOnBehalfOfToken(token),
                )
                call.respond(isSkjermet)
            }
        }

        get(apiPersonDiskresjonskodePath) {
            personRequestHandler(
                resource = apiPersonDiskresjonskodePath,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            ) {
                val personIdentNumber = getPersonIdent()?.let { requestedPersonIdent ->
                    PersonIdentNumber(requestedPersonIdent)
                } ?: throw IllegalArgumentException("No personIdentNumber supplied in header")

                val callId = getCallId()

                val response = pdlClient.person(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                )?.hentPerson?.diskresjonskode ?: ""
                call.respond(response)
            }
        }

        get(apiPersonAdressePath) {
            personRequestHandler(
                resource = apiPersonAdressePath,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            ) {
                val personIdentNumber = getPersonIdent()?.let { requestedPersonIdent ->
                    PersonIdentNumber(requestedPersonIdent)
                } ?: throw IllegalArgumentException("No personIdentNumber supplied in header")

                val callId = getCallId()

                val postinformasjonList = kodeverkClient.getPostinformasjon(callId)

                val pdlPerson = pdlClient.person(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                )
                pdlPerson?.hentPerson?.let { person ->
                    val response = PersonAdresseResponse(
                        navn = person.fullName ?: "",
                        bostedsadresse = person.hentPdlBostedsadresse()
                            ?.let { Bostedsadresse(it, postinformasjonList) },
                        kontaktadresse = person.hentPdlKontaktadresse()
                            ?.let { Kontaktadresse(it, postinformasjonList) },
                        oppholdsadresse = person.hentPdlOppholdsadresse()
                            ?.let { Oppholdsadresse(it, postinformasjonList) },
                    )
                    call.respond(response)
                } ?: call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get(apiPersonKontaktinformasjonPath) {
            personRequestHandler(
                resource = apiPersonKontaktinformasjonPath,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            ) {
                val personIdentNumber = getPersonIdent()?.let { requestedPersonIdent ->
                    PersonIdentNumber(requestedPersonIdent)
                } ?: throw IllegalArgumentException("No personIdentNumber supplied in header")

                val callId = getCallId()
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val response = krrClient.digitalKontaktinfo(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                    token = token,
                ).toSyfomodiapersonKontaktinfo()
                call.respond(response)
            }
        }

        get(apiPersonBrukerinfoPath) {
            personRequestHandler(
                resource = apiPersonBrukerinfoPath,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            ) {
                val personIdentNumber = getPersonIdent()?.let { requestedPersonIdent ->
                    PersonIdentNumber(requestedPersonIdent)
                } ?: throw IllegalArgumentException("No $NAV_PERSONIDENT_HEADER supplied in header")

                val callId = getCallId()

                val pdlIdenter = pdlClient.hentIdenter(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                )
                val aktivPersonident = pdlIdenter?.aktivIdent?.let { PersonIdentNumber(it) }
                if (aktivPersonident == null) {
                    throw IllegalArgumentException("Found no aktiv personident for supplied $NAV_PERSONIDENT_HEADER")
                }
                val pdlPerson = pdlClient.person(
                    callId = callId,
                    personIdentNumber = aktivPersonident,
                )

                pdlPerson?.hentPerson?.let { person ->
                    val response = SyfomodiapersonBrukerinfo(
                        aktivPersonident = aktivPersonident.value,
                        navn = person.fullName,
                        dodsdato = person.dodsdato,
                        tilrettelagtKommunikasjon = person.hentTilrettelagtKommunikasjon(),
                        sikkerhetstiltak = person.hentSikkerhetstiltak(),
                    )
                    call.respond(response)
                } ?: call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
