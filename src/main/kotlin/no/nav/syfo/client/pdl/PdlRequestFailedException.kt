package no.nav.syfo.client.pdl

class PdlRequestFailedException(
    message: String = "Request to get Person from PDL Failed",
) : RuntimeException(message)
