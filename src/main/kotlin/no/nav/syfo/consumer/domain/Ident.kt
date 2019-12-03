package no.nav.syfo.consumer.domain

data class Ident(
        val ident: String,
        val identgruppe: String,
        val gjeldende: Boolean
)
