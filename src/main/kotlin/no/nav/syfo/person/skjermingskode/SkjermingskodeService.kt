package no.nav.syfo.person.skjermingskode

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.person.api.domain.PersonInfo
import no.nav.syfo.person.api.domain.Skjermingskode

class SkjermingskodeService(
    private val skjermedePersonerPipClient: SkjermedePersonerPipClient,
    private val pdlClient: PdlClient,
) {
    suspend fun hentSkjermingskodeForPersonidenter(
        callId: String,
        personidenter: List<PersonIdentNumber>,
        token: String,
    ) =
        personidenter.map { personident ->
            val skjermingskode = hentBrukersSkjermingskode(
                callId = callId,
                personident = personident,
                token = token,
            )
            Pair(personident, skjermingskode)
        }.mapNotNull { (personident, skjermingskode) ->
            skjermingskode.await()?.let {
                PersonInfo(
                    fnr = personident.value,
                    skjermingskode = it,
                )
            }
        }

    private suspend fun hentBrukersSkjermingskode(
        callId: String,
        personident: PersonIdentNumber,
        token: String,
    ): Deferred<Skjermingskode?> =
        CoroutineScope(DISPATCHER).async {
            val hasAdressebeskyttelse = pdlClient.hasAdressebeskyttelse(callId = callId, personIdent = personident)
            hasAdressebeskyttelse?.let {
                if (hasAdressebeskyttelse) {
                    Skjermingskode.DISKRESJONSMERKET
                } else {
                    val isSkjermet = skjermedePersonerPipClient.isSkjermet(
                        callId = callId,
                        personIdentNumber = personident,
                        token = token,
                    )
                    if (isSkjermet) Skjermingskode.EGEN_ANSATT else Skjermingskode.INGEN
                }
            }
        }

    companion object {
        private val DISPATCHER = Dispatchers.IO.limitedParallelism(20)
    }
}
