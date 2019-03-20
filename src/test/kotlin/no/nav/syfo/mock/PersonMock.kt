package no.nav.syfo.mock

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = "mockPerson_V3", havingValue = "true")
class PersonMock : PersonV3 {

    override fun ping() {

    }

    override fun hentPerson(hentPersonRequest: HentPersonRequest?): HentPersonResponse {
        return HentPersonResponse()
                .withPerson(Person()
                        .withAktoer(PersonIdent()
                                .withIdent(NorskIdent()
                                        .withIdent("1234567890123")))
                        .withPersonnavn(Personnavn()
                                .withFornavn("Sygve")
                                .withMellomnavn("Arve")
                                .withEtternavn("Sykmeldt")))
    }

    override fun hentEkteskapshistorikk(p0: HentEkteskapshistorikkRequest?): HentEkteskapshistorikkResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hentGeografiskTilknytning(p0: HentGeografiskTilknytningRequest?): HentGeografiskTilknytningResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hentPersonerMedSammeAdresse(p0: HentPersonerMedSammeAdresseRequest?): HentPersonerMedSammeAdresseResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hentPersonhistorikk(p0: HentPersonhistorikkRequest?): HentPersonhistorikkResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hentPersonnavnBolk(p0: HentPersonnavnBolkRequest?): HentPersonnavnBolkResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hentSikkerhetstiltak(p0: HentSikkerhetstiltakRequest?): HentSikkerhetstiltakResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hentVerge(p0: HentVergeRequest?): HentVergeResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}