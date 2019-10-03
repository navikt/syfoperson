package no.nav.syfo.mock

import no.nav.syfo.config.consumer.EgenAnsattConfig
import no.nav.tjeneste.pip.egen.ansatt.v1.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = [EgenAnsattConfig.MOCK_KEY], havingValue = "true")
class EgenAnsattMock : EgenAnsattV1 {
    override fun hentErEgenAnsattEllerIFamilieMedEgenAnsatt(p0: WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest?): WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse {
        return WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse().withEgenAnsatt(false)
    }

    override fun ping() {}
}
