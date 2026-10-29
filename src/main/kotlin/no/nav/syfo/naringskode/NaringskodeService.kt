package no.nav.syfo.naringskode

import no.nav.syfo.client.ssb.SsbClient

class NaringskodeService(private val ssbClient: SsbClient) {
    suspend fun getHierarki(code: String, standard: NaringskodeStandard): NaringskodeHierarki? {
        val byCode = ssbClient.getNaringskoder(standard).associateBy { it.code }
        var current = byCode[code] ?: return null
        val hierarki = mutableListOf(current)
        while (current.parentCode != null) {
            current = byCode.getValue(current.parentCode)
            hierarki.add(current)
        }
        return NaringskodeHierarki(standard = standard.year, hierarki = hierarki)
    }
}
