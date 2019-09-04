package no.nav.syfo.consumer

import no.nav.syfo.service.SkjermingskodeService
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class EgenAnsattConsumer @Inject constructor(private val egenAnsattV1: EgenAnsattV1) : InitializingBean {

    private var instance: EgenAnsattConsumer? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    fun egenAnsattConsumer(): EgenAnsattConsumer? {
        return instance
    }

    @Cacheable(cacheNames = ["egenAnsattByFnr"], key = "#fnr", condition = "#fnr != null")
    fun erEgenAnsatt(fnr: String): Boolean {
        try {
            return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                    .withIdent(fnr)
            ).isEgenAnsatt
        } catch (e: RuntimeException) {
            LOG.error("Klarte ikke hente egenansatt status på sykmeldt")
            throw e
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SkjermingskodeService::class.java)
    }

}
