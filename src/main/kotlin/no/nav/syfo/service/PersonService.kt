package no.nav.syfo.service

import no.nav.syfo.consumer.AktorRestConsumer
import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerId
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.text.WordUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.Optional.ofNullable
import javax.inject.Inject
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotFoundException

@Service
class PersonService @Inject constructor(
        private val personV3: PersonV3,
        private val aktoerRestConsumer: AktorRestConsumer
) : InitializingBean {
    private var instance: PersonService? = null

    companion object {
        private val LOG = LoggerFactory.getLogger(PersonService::class.java)
    }

    override fun afterPropertiesSet() {
        instance = this
    }

    fun personService() = instance

    private val KODE6 = "SPSF"
    private val KODE7 = "SPFO"

    @Cacheable(cacheNames = ["personByFnr"], key = "#fnr", condition = "#fnr != null")
    fun hentPersonFraFnr(fnr: String): Person {
        if (StringUtils.isBlank(fnr) || !fnr.matches("\\d{11}$".toRegex())) throw IllegalArgumentException()
        try {
            return personV3.hentPerson(HentPersonRequest()
                    .withAktoer(AktoerId()
                            .withAktoerId(aktoerRestConsumer.getAktorId(fnr))))
                    .person
        } catch (e: HentPersonSikkerhetsbegrensning) {
            LOG.info("Fikk sikkerhetsbegrensing mot TPS på henting av person")
            throw ForbiddenException()
        } catch (e: HentPersonPersonIkkeFunnet) {
            LOG.info("Klarte ikke finne person med fnr i TPS")
            throw NotFoundException()
        } catch (e: RuntimeException) {
            LOG.info("Fikk RuntimeException på henting av person fra TPS")
            throw e
        }
    }

    fun hentNavnFraPerson(person: Person): String {
        val navnFraTPS = person.personnavn
        val mellomnavn = navnFraTPS.mellomnavn ?: ""
        val fulltNavn = navnFraTPS.fornavn + " " + mellomnavn + " " + navnFraTPS.etternavn
        return WordUtils.capitalize(fulltNavn.toLowerCase(), '-', ' ')
    }

    fun erBrukerDiskresjonsmerket(person: Person): Boolean {
        val diskresjonskode = ofNullable(person.diskresjonskode).map { it.value }.orElse("")
        return KODE6 == diskresjonskode || KODE7 == diskresjonskode
    }
}
