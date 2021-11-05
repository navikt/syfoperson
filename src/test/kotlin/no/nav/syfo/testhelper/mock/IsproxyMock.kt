package no.nav.syfo.testhelper.mock

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.syketilfelle.*
import no.nav.syfo.client.syketilfelle.SyketilfelleClient.Companion.ISPROXY_SYFOSYKETILFELLE_OPPFOLGINGSTILFELLE_PERSON_NO_ARBEIDSGIVER_PATH
import no.nav.syfo.domain.AktorId
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER_DEFAULT
import no.nav.syfo.testhelper.getRandomPort
import java.time.LocalDate
import java.time.LocalDateTime

fun kOppfolgingstilfelleDTO(
    aktorId: AktorId = ARBEIDSTAKER_AKTORID,
) = KOppfolgingstilfelleDTO(
    aktorId = aktorId.value,
    orgnummer = VIRKSOMHETSNUMMER_DEFAULT.value,
    tidslinje = listOf(
        KSyketilfelledagDTO(
            dag = LocalDate.now().minusDays(10),
            prioritertSyketilfellebit = null,
        ),
        KSyketilfelledagDTO(
            dag = LocalDate.now().plusDays(10),
            prioritertSyketilfellebit = null,
        ),
    ),
    sisteDagIArbeidsgiverperiode = KSyketilfelledagDTO(
        dag = LocalDate.now().plusDays(10),
        prioritertSyketilfellebit = null,
    ),
    antallBrukteDager = 0,
    oppbruktArbeidsgvierperiode = false,
    utsendelsestidspunkt = LocalDateTime.now(),
)

fun kOppfolgingstilfellePersonDTO(
    aktorId: AktorId = ARBEIDSTAKER_AKTORID,
) = KOppfolgingstilfellePersonDTO(
    aktorId = aktorId.value,
    tidslinje = listOf(
        KSyketilfelledagDTO(
            dag = LocalDate.now().minusDays(10),
            prioritertSyketilfellebit = null,
        ),
        KSyketilfelledagDTO(
            dag = LocalDate.now().plusDays(10),
            prioritertSyketilfellebit = null,
        ),
    ),
    sisteDagIArbeidsgiverperiode = KSyketilfelledagDTO(
        dag = LocalDate.now().plusDays(10),
        prioritertSyketilfellebit = null,
    ),
    antallBrukteDager = 0,
    oppbruktArbeidsgvierperiode = false,
    utsendelsestidspunkt = LocalDateTime.now(),
)

class IsproxyMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val name = "isproxy"
    val server = mockServer()

    private fun mockServer(): NettyApplicationEngine {
        return embeddedServer(
            factory = Netty,
            port = port
        ) {
            installContentNegotiation()
            routing {
                get("${SyketilfelleClient.ISPROXY_SYFOSYKETILFELLE_OPPFOLGINGSTILFELLE_PERSON_PATH}/${ARBEIDSTAKER_AKTORID.value}/${VIRKSOMHETSNUMMER_DEFAULT.value}") {
                    call.respond(
                        kOppfolgingstilfelleDTO(
                            aktorId = ARBEIDSTAKER_AKTORID,
                        )
                    )
                }
                get("${SyketilfelleClient.ISPROXY_SYFOSYKETILFELLE_OPPFOLGINGSTILFELLE_PERSON_PATH}/${ARBEIDSTAKER_AKTORID.value}$ISPROXY_SYFOSYKETILFELLE_OPPFOLGINGSTILFELLE_PERSON_NO_ARBEIDSGIVER_PATH") {
                    call.respond(
                        kOppfolgingstilfellePersonDTO(
                            aktorId = ARBEIDSTAKER_AKTORID,
                        )
                    )
                }
            }
        }
    }
}
