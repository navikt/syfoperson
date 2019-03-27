package no.nav.syfo.consumer

import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import no.nav.tjeneste.virksomhet.aktoer.v2.HentAktoerIdForIdentPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentAktoerIdForIdentRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import javax.ws.rs.NotFoundException

@Component
class AktoerConsumer
constructor(private val aktoerV2: AktoerV2) : InitializingBean {

    override fun afterPropertiesSet() { instance = this }

    fun hentAktoerIdForFnr(fnr: String): String {
        try {
            return aktoerV2.hentAktoerIdForIdent(WSHentAktoerIdForIdentRequest()
                    .withIdent(fnr)
            ).aktoerId
        } catch (e: HentAktoerIdForIdentPersonIkkeFunnet) {
            LOG.info("Klarte ikke finne aktorId for fnr")
            throw NotFoundException()
        } catch (e: RuntimeException) {
            LOG.info("Fikk RuntimeException på henting av aktorid for fnr")
            throw e
        }
    }

    companion object {
        private var instance: AktoerConsumer? = null
        private val LOG = LoggerFactory.getLogger(AktoerConsumer::class.java)

        fun aktoerConsumer(): AktoerConsumer? { return instance }
    }
}