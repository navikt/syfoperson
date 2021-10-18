package no.nav.syfo.person.oppfolgingstilfelle

import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.syketilfelle.*
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.domain.Virksomhetsnummer

class OppfolgingstilfelleService(
    private val pdlClient: PdlClient,
    private val syketilfelleClient: SyketilfelleClient,
) {
    suspend fun oppfolgingstilfellePersonArbeidsgiver(
        callId: String,
        personIdentNumber: PersonIdentNumber,
        token: String,
        virksomhetsnummer: Virksomhetsnummer,
    ): KOppfolgingstilfelleDTO? {
        val personAktorId = pdlClient.aktorId(
            personIdentNumber = personIdentNumber,
            callId = callId,
        )
        return syketilfelleClient.getOppfolgingstilfellePersonArbeidsgiver(
            aktorId = personAktorId,
            callId = callId,
            token = token,
            virksomhetsnummer = virksomhetsnummer,
        )
    }

    suspend fun oppfolgingstilfellePersonUtenArbeidsgiver(
        callId: String,
        personIdentNumber: PersonIdentNumber,
        token: String,
    ): KOppfolgingstilfellePersonDTO? {
        val personAktorId = pdlClient.aktorId(
            callId = callId,
            personIdentNumber = personIdentNumber,
        )
        return syketilfelleClient.getOppfolgingstilfellePerson(
            aktorId = personAktorId,
            callId = callId,
            token = token,
        )
    }
}
