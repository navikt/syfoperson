package no.nav.syfo.person.oppfolgingstilfelle

import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.syketilfelle.*
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.person.api.domain.Fnr
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class OppfolgingstilfelleService @Inject constructor(
    private val pdlConsumer: PdlConsumer,
    private val syketilfelleConsumer: SyketilfelleConsumer
) {
    fun oppfolgingstilfellePersonArbeidsgiver(
        callId: String,
        personIdent: Fnr,
        virksomhetsnummer: Virksomhetsnummer,
    ): KOppfolgingstilfelle? {
        val personAktorId = pdlConsumer.aktorId(
            personIdent = personIdent,
            callId = callId,
        )
        return syketilfelleConsumer.getOppfolgingstilfellePersonArbeidsgiver(
            aktorId = personAktorId,
            callId = callId,
            virksomhetsnummer = virksomhetsnummer,
        )
    }

    fun oppfolgingstilfellePersonUtenArbeidsgiver(
        callId: String,
        personIdent: Fnr,
    ): KOppfolgingstilfellePerson? {
        val personAktorId = pdlConsumer.aktorId(
            personIdent = personIdent,
            callId = callId,
        )
        return syketilfelleConsumer.getOppfolgingstilfellePerson(
            aktorId = personAktorId,
            callId = callId,
        )
    }
}
