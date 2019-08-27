package no.nav.syfo.service

import com.nimbusds.oauth2.sdk.util.StringUtils.isBlank
import no.nav.syfo.consumer.AktoerConsumer
import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerId
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.text.WordUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.util.Optional.ofNullable
import javax.inject.Inject
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotFoundException

@Component
class PersonService @Inject constructor(private val personV3: PersonV3, private val aktoerConsumer: AktoerConsumer) : InitializingBean {
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

    @Cacheable("fnr-til-navn")
    fun hentNavnFraFnr(fnr: String): String {
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

    fun erBrukerDiskresjonsmerket(aktoerId: String): Boolean {
        val diskresjonskode = hentDiskresjonskodeForAktoer(aktoerId)
        return KODE6 == diskresjonskode || KODE7 == diskresjonskode
    }

    fun hentDiskresjonskodeForAktoer(aktoerId: String): String {
        if (isBlank(aktoerId) || !aktoerId.matches("\\d{13}$".toRegex())) {
            LOG.error("Ugyldig format på aktoerId: $aktoerId")
            throw IllegalArgumentException()
        }
        try {
            val person = personV3.hentPerson(HentPersonRequest()
                    .withAktoer(AktoerId()
                            .withAktoerId(aktoerId)))
                    .person
            return ofNullable(person.diskresjonskode).map { it.value }.orElse("")
        } catch (e: HentPersonSikkerhetsbegrensning) {
            LOG.error("Fikk sikkerhetsbegrensing ved oppslag med aktoerId: $aktoerId")
            throw ForbiddenException()
        } catch (e: HentPersonPersonIkkeFunnet) {
            LOG.error("Fant ikke person med aktoerId: $aktoerId")
            throw RuntimeException()
        } catch (e: RuntimeException) {
            LOG.error("Fikk RuntimeException mot TPS for diskresjonskode ved oppslag av aktoerId: $aktoerId")
            return ""
        }
    }
}
