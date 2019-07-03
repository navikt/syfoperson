package no.nav.syfo.service

import no.nav.syfo.consumer.AktoerConsumer
import no.nav.syfo.consumer.EgenAnsattConsumer
import no.nav.syfo.controller.domain.Skjermingskode
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class SkjermingskodeService @Inject constructor(
        private val aktoerConsumer: AktoerConsumer,
        private val egenAnsattConsumer: EgenAnsattConsumer,
        private val personService: PersonService
) : InitializingBean {
    private var instance: SkjermingskodeService? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    fun hentBrukersSkjermingskode(fnr: String): Skjermingskode {
        if (personService.erBrukerDiskresjonsmerket(aktoerConsumer.hentAktoerIdForFnr(fnr)))
            return Skjermingskode.DISKRESJONSMERKET
        return if (egenAnsattConsumer.erEgenAnsatt(fnr)) Skjermingskode.EGEN_ANSATT else Skjermingskode.INGEN
    }
}
