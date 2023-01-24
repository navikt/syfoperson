package no.nav.syfo.testhelper.mock

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.pdl.*
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_DOD
import no.nav.syfo.testhelper.getRandomPort
import java.time.LocalDate

fun generatePdlPersonResponse(
    gradering: Gradering? = null,
    doedsdato: LocalDate? = null,
) = PdlPersonResponse(
    errors = null,
    data = generatePdlHentPerson(
        pdlPersonNavn = generatePdlPersonNavn(),
        adressebeskyttelse = generateAdressebeskyttelse(gradering = gradering),
        doedsdato = doedsdato,
    )
)

fun generatePdlPersonNavn(): PdlPersonNavn {
    return PdlPersonNavn(
        fornavn = UserConstants.PERSON_NAME_FIRST,
        mellomnavn = UserConstants.PERSON_NAME_MIDDLE,
        etternavn = UserConstants.PERSON_NAME_LAST,
    )
}

fun generateAdressebeskyttelse(
    gradering: Gradering? = null
): Adressebeskyttelse {
    return Adressebeskyttelse(
        gradering = gradering ?: Gradering.UGRADERT
    )
}

fun generatePdlHentPerson(
    pdlPersonNavn: PdlPersonNavn?,
    adressebeskyttelse: Adressebeskyttelse? = null,
    doedsdato: LocalDate? = null,
): PdlHentPerson {
    return PdlHentPerson(
        hentPerson = PdlPerson(
            navn = listOf(
                pdlPersonNavn ?: generatePdlPersonNavn()
            ),
            adressebeskyttelse = listOf(
                adressebeskyttelse ?: generateAdressebeskyttelse()
            ),
            bostedsadresse = null,
            kontaktadresse = null,
            oppholdsadresse = null,
            doedsfall = if (doedsdato == null) emptyList() else {
                listOf(PdlDoedsfall(doedsdato))
            },
        )
    )
}

class PdlMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val name = "pdl"
    val personResponseDefault = generatePdlPersonResponse()

    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            post {
                val pdlRequest = call.receive<PdlRequest>()
                call.respond(
                    if (ARBEIDSTAKER_ADRESSEBESKYTTET.value == pdlRequest.variables.ident) {
                        generatePdlPersonResponse(Gradering.STRENGT_FORTROLIG)
                    } else if (ARBEIDSTAKER_DOD.value == pdlRequest.variables.ident) {
                        generatePdlPersonResponse(doedsdato = LocalDate.now())
                    } else {
                        personResponseDefault
                    }
                )
            }
        }
    }
}
