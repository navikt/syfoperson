package no.nav.syfo.consumer

import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import no.nav.tjeneste.virksomhet.aktoer.v2.HentAktoerIdForIdentPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentAktoerIdForIdentRequest
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.lang.RuntimeException

@Component
class AktoerConsumer
constructor(private val aktoerV2: AktoerV2) : InitializingBean {

    override fun afterPropertiesSet() {
        instance = this
    }

    fun hentAktoerIdForFnr(fnr: String): String {
        try {
            return aktoerV2.hentAktoerIdForIdent(WSHentAktoerIdForIdentRequest()
                    .withIdent(fnr)
            ).aktoerId
        } catch (e: HentAktoerIdForIdentPersonIkkeFunnet) {
            throw RuntimeException(e)
        }

    }

    fun hentFnrForAktoerId(aktoerId: String): String {
        try {
            return aktoerV2.hentIdentForAktoerId(
                    WSHentIdentForAktoerIdRequest()
                            .withAktoerId(aktoerId)
            ).ident
        } catch (e: HentIdentForAktoerIdPersonIkkeFunnet) {
            throw RuntimeException(e)
        }

    }

    companion object {

        private var instance: AktoerConsumer? = null

        fun aktoerConsumer(): AktoerConsumer? {
            return instance
        }
    }
}