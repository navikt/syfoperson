package no.nav.syfo.person.api

import com.fasterxml.jackson.core.type.TypeReference
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.client.krr.KRRClient
import no.nav.syfo.client.krr.toSyfomodiapersonKontaktinfo
import no.nav.syfo.client.pdl.*
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

private val objectMapper = configuredJacksonMapper()

fun Route.registrerPersonApi(
    krrClient: KRRClient,
    pdlClient: PdlClient,
    skjermingskodeService: SkjermingskodeService,
    skjermedePersonerPipClient: SkjermedePersonerPipClient,
    veilederTilgangskontrollClient: VeilederTilgangskontrollClient,
) {
    route(apiPersonBasePath) {
        post(apiPersonInfoPath) {
            try {
                val json = call.receive<String>()
                val list = objectMapper.readValue(json, object : TypeReference<List<PersonInfoRequest>>() {})
                val personIdentNumberList = list.map { personIdent ->
                    PersonIdentNumber(personIdent.fnr)
                }

                val callId = getCallId()
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val grantedAccessList = veilederTilgangskontrollClient.hasVeilederAccessToPersonList(
                    callId = callId,
                    personIdentNumberList = personIdentNumberList,
                    token = token,
                )
                val response: List<PersonInfo> = grantedAccessList
                    .map { personIdentNumber ->
                        val person = pdlClient.person(
                            callId = callId,
                            personIdentNumber = personIdentNumber,
                        )
                        val skjermingskode = skjermingskodeService.hentBrukersSkjermingskode(
                            callId = callId,
                            person = person,
                            personIdent = personIdentNumber,
                            token = token,
                        )
                        PersonInfo(
                            fnr = personIdentNumber.value,
                            skjermingskode = skjermingskode
                        )
                    }
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
                )?.getFullName() ?: ""

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
                    token = token
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
                )?.getDiskresjonskode() ?: ""
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

                val person = pdlClient.person(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                )
                val response = PersonAdresseResponse(
                    navn = person?.getFullName() ?: "",
                    bostedsadresse = person?.bostedsadresse(),
                    kontaktadresse = person?.kontaktadresse(),
                    oppholdsadresse = person?.oppholdsadresse()
                )
                call.respond(response)
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
                )
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
                } ?: throw IllegalArgumentException("No personIdentNumber supplied in header")

                val callId = getCallId()
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val kontaktinfo = krrClient.digitalKontaktinfo(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                    token = token
                ).toSyfomodiapersonKontaktinfo()

                val name = pdlClient.person(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                )?.getFullName()

                val response = SyfomodiapersonBrukerinfo(
                    navn = name,
                    kontaktinfo = kontaktinfo
                )
                call.respond(response)
            }
        }
    }
}
