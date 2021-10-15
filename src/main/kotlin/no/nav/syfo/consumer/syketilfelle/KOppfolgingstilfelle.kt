package no.nav.syfo.consumer.syketilfelle

import no.nav.syfo.person.api.domain.OppfolgingstilfelleDTO
import java.time.LocalDateTime

data class KOppfolgingstilfelle(
    val aktorId: String,
    val orgnummer: String,
    val tidslinje: List<KSyketilfelledag>,
    val sisteDagIArbeidsgiverperiode: KSyketilfelledag,
    val antallBrukteDager: Int,
    val oppbruktArbeidsgvierperiode: Boolean,
    val utsendelsestidspunkt: LocalDateTime,
)

fun KOppfolgingstilfelle.toOppfolgingstilfelleDTO() =
    OppfolgingstilfelleDTO(
        virksomhetsnummer = this.orgnummer,
        fom = this.tidslinje.first().dag,
        tom = this.tidslinje.last().dag,
    )
