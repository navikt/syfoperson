package no.nav.syfo.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class StringUtilTest {

    @Test
    fun `Should capitalize navn`() {
        assertEquals("Ola", "OLA".lowerCapitalize())
    }

    @Test
    fun `Should capitalize navn with space`() {
        assertEquals("Jan Ola", "JAN OLA".lowerCapitalize())
    }

    @Test
    fun `Should capitalize navn with dashes`() {
        assertEquals("Jan-Ola", "JAN-OLA".lowerCapitalize())
    }

    @Test
    fun `Should capitalize navn with dashes and spaces`() {
        assertEquals("Jan-Ola Jon Ola-Jan Jon Jan-O-Jul", "JAN-OLA JON OLA-JAN JON JAN-O-JUL".lowerCapitalize())
    }
}
