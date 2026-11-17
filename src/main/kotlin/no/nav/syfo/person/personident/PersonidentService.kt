package no.nav.syfo.person.personident

import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.domain.PersonIdentNumber

class PersonidentService(
    private val pdlClient: PdlClient,
) {
    suspend fun getFolkeregisteridenter(
        personident: PersonIdentNumber,
        callId: String,
    ): List<PersonIdentNumber> {
        val folkeregisteridenter = pdlClient.person(
            callId = callId,
            personIdentNumber = personident,
        )?.hentPerson?.folkeregisteridentifikator
            ?: throw RuntimeException("Found no person in PDL for supplied personident")

        return (folkeregisteridenter.map { PersonIdentNumber(it.identifikasjonsnummer) } + personident)
            .distinctBy { it.value }
    }
}
