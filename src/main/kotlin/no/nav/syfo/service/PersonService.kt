package no.nav.syfo.service

import org.slf4j.LoggerFactory.getLogger
import no.nav.syfo.consumer.AktoerConsumer
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerId
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.text.WordUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotFoundException

@Component
class PersonService @Inject constructor(private val personV3: PersonV3, private val aktoerConsumer: AktoerConsumer): InitializingBean {
    private var instance: PersonService? = null
    private val KODE6 = "SPSF"
    private val KODE7 = "SPFO"

    companion object {
        private val LOG = LoggerFactory.getLogger(PersonService::class.java)
    }

    override fun afterPropertiesSet() { instance = this }

    fun personService() : PersonService? { return instance }

    fun hentNavnFraFnr(fnr: String) : String {
        if (StringUtils.isBlank(fnr) || !fnr.matches("\\d{11}$".toRegex())) throw IllegalArgumentException()
        try {
            val person: Person = personV3.hentPerson(HentPersonRequest()
                    .withAktoer(AktoerId()
                            .withAktoerId(aktoerConsumer.hentAktoerIdForFnr(fnr))))
                    .person
            val navnFraTPS = person.personnavn
            val mellomnavn = navnFraTPS.mellomnavn ?: ""
            val fulltNavn = navnFraTPS.fornavn + " " + mellomnavn + " " + navnFraTPS.etternavn
            return WordUtils.capitalize(fulltNavn.toLowerCase(), '-', ' ')
        } catch (e: HentPersonSikkerhetsbegrensning) {
            LOG.info("Fikk sikkerhetsbegrensing mot TPS på henting av navn")
            throw ForbiddenException()
        } catch (e: HentPersonPersonIkkeFunnet) {
            LOG.info("Klarte ikke finne navn på person med fnr i TPS")
            throw NotFoundException()
        } catch (e: RuntimeException) {
            LOG.info("Fikk RuntimeException på henting av person navn fra TPS")
            return ""
        }
    }

}