package no.nav.syfo.sektorkode

import no.nav.syfo.client.ssb.SsbClient
import no.nav.syfo.client.ssb.SsbException

class SektorkodeService(private val ssbClient: SsbClient) {
    suspend fun getHierarki(code: String, standard: SektorkodeStandard): SektorkodeHierarki? {
        val byCode = ssbClient.getSektorkoder(standard).associateBy { it.code }
        var current = byCode[code] ?: return null
        val hierarki = mutableListOf(current)
        while (current.parentCode != null) {
            if (hierarki.size >= standard.levels) invalidHierarchy()
            val parent = byCode[current.parentCode] ?: invalidHierarchy()
            if (parent.level != current.level - 1) invalidHierarchy()
            current = parent
            hierarki.add(current)
        }
        if (current.level != 1) invalidHierarchy()
        return SektorkodeHierarki(standard = standard.year, hierarki = hierarki)
    }

    private fun invalidHierarchy(): Nothing =
        throw SsbException("Invalid SSB sektorkode hierarchy", IllegalArgumentException("Invalid parent chain"))
}
