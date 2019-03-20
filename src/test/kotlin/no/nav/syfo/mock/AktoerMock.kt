package no.nav.syfo.mock

import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = "mockAktoer_V2", havingValue = "true")
class AktoerMock : AktoerV2 {

    override fun ping() {

    }

    override fun hentAktoerIdForIdent(p0: WSHentAktoerIdForIdentRequest?): WSHentAktoerIdForIdentResponse {
        return WSHentAktoerIdForIdentResponse().withAktoerId("12345678910123")
    }

    override fun hentIdentForAktoerId(p0: WSHentIdentForAktoerIdRequest?): WSHentIdentForAktoerIdResponse {
        return WSHentIdentForAktoerIdResponse().withIdent("123456789101")
    }

    override fun hentAktoerIdForIdentListe(p0: WSHentAktoerIdForIdentListeRequest?): WSHentAktoerIdForIdentListeResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hentIdentForAktoerIdListe(p0: WSHentIdentForAktoerIdListeRequest?): WSHentIdentForAktoerIdListeResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}