package no.nav.syfo.testhelper.mock

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.krr.*
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.getRandomPort

fun digitalKontaktinfoBolkKanVarslesTrue(personIdentNumber: String) = DigitalKontaktinfoBolk(
    personer = mapOf(
        personIdentNumber to DigitalKontaktinfo(
            epostadresse = UserConstants.PERSON_EMAIL,
            kanVarsles = true,
            reservert = false,
            mobiltelefonnummer = UserConstants.PERSON_TLF,
            aktiv = true,
            personident = personIdentNumber,
        )
    ),
)

class KrrMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val name = "krr"
    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            post(KRRClient.KRR_KONTAKTINFORMASJON_BOLK_PATH) {
                val krrRequestBody = call.receive<DigitalKontaktinfoBolkRequestBody>()
                call.respond(
                    digitalKontaktinfoBolkKanVarslesTrue(
                        personIdentNumber = krrRequestBody.personidenter.first(),
                    )
                )
            }
        }
    }
}
