package no.nav.syfo.person.Oppfolgingstilfelle

import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.syketilfelle.SyketilfelleConsumer
import no.nav.syfo.consumer.syketilfelle.domain.KOppfolgingstilfellePerson
import no.nav.syfo.person.api.domain.Fnr
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class OppfolgingstilfelleService @Inject constructor(
    private val pdlConsumer: PdlConsumer,
    private val syketilfelleConsumer: SyketilfelleConsumer
) {
    fun oppfolgingstilfelle(
        personIdent: Fnr,
        callId: String,
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
