package no.nav.syfo.util

import java.time.LocalDate

fun LocalDate.isBeforeOrEqual(other: LocalDate): Boolean {
    return this.isBefore(other) || this.isEqual(other)
}

fun LocalDate.isAfterOrEqual(other: LocalDate): Boolean {
    return this.isAfter(other) || this.isEqual(other)
}
