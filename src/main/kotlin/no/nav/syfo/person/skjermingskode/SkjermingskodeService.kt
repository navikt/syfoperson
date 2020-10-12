package no.nav.syfo.person.skjermingskode

import no.nav.syfo.person.api.domain.Skjermingskode
import no.nav.syfo.consumer.pdl.PdlHentPerson
import no.nav.syfo.consumer.pdl.isKode6Or7
import no.nav.syfo.consumer.skjermedepersoner.SkjermedePersonerPipConsumer
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class SkjermingskodeService @Inject constructor(
    private val skjermedePersonerPipConsumer: SkjermedePersonerPipConsumer
) : InitializingBean {
    private var instance: SkjermingskodeService? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    fun hentBrukersSkjermingskode(person: PdlHentPerson?, fnr: String): Skjermingskode {
        if (person?.isKode6Or7() == true)
            return Skjermingskode.DISKRESJONSMERKET
        return if (skjermedePersonerPipConsumer.erSkjermet(fnr)) Skjermingskode.EGEN_ANSATT else Skjermingskode.INGEN
    }
}
