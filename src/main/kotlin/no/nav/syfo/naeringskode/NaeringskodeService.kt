package no.nav.syfo.naeringskode

import no.nav.syfo.client.ssb.SsbClient

class NaeringskodeService(private val ssbClient: SsbClient) {
    suspend fun getHierarki(code: String, standard: NaeringskodeStandard): NaeringskodeHierarki? {
        val byCode = ssbClient.getNaeringskoder(standard).associateBy { it.code }
        var current = byCode[code] ?: return null
        val hierarki = mutableListOf(current)
        while (current.parentCode != null) {
            current = byCode.getValue(current.parentCode)
            hierarki.add(current)
        }
        return NaeringskodeHierarki(standard = standard.year, hierarki = hierarki)
    }
}
