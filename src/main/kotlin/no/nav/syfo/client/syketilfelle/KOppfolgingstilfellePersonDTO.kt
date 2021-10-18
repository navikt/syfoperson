package no.nav.syfo.client.syketilfelle

import no.nav.syfo.person.api.domain.OppfolgingstilfellePersonDTO
import java.time.LocalDateTime

data class KOppfolgingstilfellePersonDTO(
    val aktorId: String,
    val tidslinje: List<KSyketilfelledagDTO>,
    val sisteDagIArbeidsgiverperiode: KSyketilfelledagDTO,
    val antallBrukteDager: Int,
    val oppbruktArbeidsgvierperiode: Boolean,
    val utsendelsestidspunkt: LocalDateTime,
)

fun KOppfolgingstilfellePersonDTO.toOppfolgingstilfellePersonDTO() =
    OppfolgingstilfellePersonDTO(
        fom = this.tidslinje.first().dag,
        tom = this.tidslinje.last().dag
    )
