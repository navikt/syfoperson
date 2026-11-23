package no.nav.syfo.sektorkode

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.client.ssb.SsbClient
import no.nav.syfo.client.ssb.SsbException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SektorkodeServiceTest {
    private val ssbClient = mockk<SsbClient>()
    private val service = SektorkodeService(ssbClient)

    @Test
    fun `rejects a cycle in the selected parent chain`() {
        coEvery { ssbClient.getSektorkoder(SektorkodeStandard.IS2012) } returns listOf(
            Sektorkode("A", "A", "B", 2),
            Sektorkode("B", "B", "A", 2),
        )

        assertThrows(SsbException::class.java) {
            runBlocking { service.getHierarki("A", SektorkodeStandard.IS2012) }
        }
    }

    @Test
    fun `rejects an orphaned parent in the selected chain`() {
        coEvery { ssbClient.getSektorkoder(SektorkodeStandard.IS1987) } returns listOf(
            Sektorkode("110", "Stats- og trygdeforvaltningen", "MISSING", 3),
        )

        assertThrows(SsbException::class.java) {
            runBlocking { service.getHierarki("110", SektorkodeStandard.IS1987) }
        }
    }

    @Test
    fun `rejects an invalid level transition`() {
        coEvery { ssbClient.getSektorkoder(SektorkodeStandard.IS2012) } returns listOf(
            Sektorkode("INNL", "Innenlandske sektorer", null, 1),
            Sektorkode("6100", "Statsforvaltningen", "INNL", 3),
        )

        assertThrows(SsbException::class.java) {
            runBlocking { service.getHierarki("6100", SektorkodeStandard.IS2012) }
        }
    }

    @Test
    fun `returns null when the code does not exist`() {
        coEvery { ssbClient.getSektorkoder(SektorkodeStandard.IS2012) } returns listOf(
            Sektorkode("INNL", "Innenlandske sektorer", null, 1),
        )

        assertEquals(null, runBlocking { service.getHierarki("missing", SektorkodeStandard.IS2012) })
    }
}
