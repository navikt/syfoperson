package no.nav.syfo.consumer.syketilfelle.domain

import no.nav.syfo.person.api.domain.OppfolgingstilfellePersonDTO
import java.time.LocalDateTime

data class KOppfolgingstilfellePerson(
    val aktorId: String,
    val tidslinje: List<KSyketilfelledag>,
    val sisteDagIArbeidsgiverperiode: KSyketilfelledag,
    val antallBrukteDager: Int,
    val oppbruktArbeidsgvierperiode: Boolean,
    val utsendelsestidspunkt: LocalDateTime,
)

fun KOppfolgingstilfellePerson.toOppfolgingstilfellePersonDTO() =
    OppfolgingstilfellePersonDTO(
        fom = this.tidslinje.first().dag,
        tom = this.tidslinje.last().dag,
    )
