package no.nav.syfo.client.syketilfelle

import no.nav.syfo.person.api.domain.OppfolgingstilfelleDTO
import java.time.LocalDateTime

data class KOppfolgingstilfelleDTO(
    val aktorId: String,
    val orgnummer: String,
    val tidslinje: List<KSyketilfelledagDTO>,
    val sisteDagIArbeidsgiverperiode: KSyketilfelledagDTO,
    val antallBrukteDager: Int,
    val oppbruktArbeidsgvierperiode: Boolean,
    val utsendelsestidspunkt: LocalDateTime,
)

fun KOppfolgingstilfelleDTO.toOppfolgingstilfelleDTO() =
    OppfolgingstilfelleDTO(
        virksomhetsnummer = this.orgnummer,
        fom = this.tidslinje.first().dag,
        tom = this.tidslinje.last().dag,
    )
