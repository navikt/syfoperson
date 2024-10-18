package no.nav.syfo.person.skjermingskode

import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.person.api.domain.Skjermingskode

class SkjermingskodeService(
    private val skjermedePersonerPipClient: SkjermedePersonerPipClient,
    private val pdlClient: PdlClient,
) {
    suspend fun hentBrukersSkjermingskode(
        callId: String,
        personIdent: PersonIdentNumber,
        token: String,
    ): Skjermingskode? {
        val hasAdressebeskyttelse = pdlClient.hasAdressebeskyttelse(callId = callId, personIdent = personIdent)

        return hasAdressebeskyttelse?.let {
            when {
                hasAdressebeskyttelse -> Skjermingskode.DISKRESJONSMERKET
                else -> if (
                    skjermedePersonerPipClient.isSkjermet(
                        callId = callId,
                        personIdentNumber = personIdent,
                        token = token,
                    )
                ) Skjermingskode.EGEN_ANSATT else Skjermingskode.INGEN
            }
        }
    }
}
