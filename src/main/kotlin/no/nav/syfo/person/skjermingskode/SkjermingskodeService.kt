package no.nav.syfo.person.skjermingskode

import no.nav.syfo.client.pdl.PdlPerson
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.person.api.domain.Skjermingskode

class SkjermingskodeService(
    private val skjermedePersonerPipClient: SkjermedePersonerPipClient,
) {
    suspend fun hentBrukersSkjermingskode(
        callId: String,
        person: PdlPerson,
        personIdent: PersonIdentNumber,
        token: String,
    ): Skjermingskode {
        if (person.isKode6Or7)
            return Skjermingskode.DISKRESJONSMERKET
        return if (
            skjermedePersonerPipClient.isSkjermet(
                callId = callId,
                personIdentNumber = personIdent,
                token = token,
            )
        ) Skjermingskode.EGEN_ANSATT else Skjermingskode.INGEN
    }
}
