package no.nav.syfo.service

import no.nav.syfo.consumer.EgenAnsattConsumer
import no.nav.syfo.controller.domain.Skjermingskode
import no.nav.syfo.pdl.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class SkjermingskodeService @Inject constructor(
        private val egenAnsattConsumer: EgenAnsattConsumer
) : InitializingBean {
    private var instance: SkjermingskodeService? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    fun hentBrukersSkjermingskode(person: PdlHentPerson?, fnr: String): Skjermingskode {
        if (person?.isKode6Or7() == true)
            return Skjermingskode.DISKRESJONSMERKET
        return if (egenAnsattConsumer.isEgenAnsatt(fnr)) Skjermingskode.EGEN_ANSATT else Skjermingskode.INGEN
    }
}
