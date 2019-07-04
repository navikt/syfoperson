package no.nav.syfo.mock

import no.nav.tjeneste.pip.egen.ansatt.v1.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["mockEgenAnsatt_V1"], havingValue = "true")
class EgenAnsattMock : EgenAnsattV1 {
    override fun hentErEgenAnsattEllerIFamilieMedEgenAnsatt(p0: WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest?): WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse {
        return WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse().withEgenAnsatt(false)
    }

    override fun ping() {}
}
