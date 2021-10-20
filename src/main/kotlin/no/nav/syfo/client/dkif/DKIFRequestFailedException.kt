package no.nav.syfo.client.dkif

const val dkifErrorMessage = "Request to get Kontakinformasjon from DKIF Failed"

class DKIFRequestFailedException(
    message: String = ""
) : RuntimeException("$dkifErrorMessage $message")
