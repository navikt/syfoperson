package no.nav.syfo.util

import org.springframework.util.MultiValueMap
import java.util.*

const val NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id"
const val APP_CONSUMER_ID = "syfoperson"

const val NAV_PERSONIDENT_HEADER = "Nav-Personident"
const val NAV_PERSONIDENTER_HEADER = "Nav-Personidenter"
const val NAV_CALL_ID_HEADER = "Nav-Call-Id"
const val TEMA_HEADER = "Tema"
const val ALLE_TEMA_HEADERVERDI = "GEN"
const val NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token"
const val MDC_CALL_ID = "callId"

fun createCallId(): String = UUID.randomUUID().toString()

fun MultiValueMap<String, String>.getPersonIdent(): String? =
    this.getFirst(NAV_PERSONIDENT_HEADER.toLowerCase())
