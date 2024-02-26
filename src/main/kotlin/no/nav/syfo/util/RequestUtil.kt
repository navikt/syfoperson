package no.nav.syfo.util

import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments

const val NAV_CALL_ID_HEADER = "Nav-Call-Id"

const val NAV_PERSONIDENT_HEADER = "nav-personident"

const val NAV_PERSONIDENTER_HEADER = "Nav-Personidenter"

const val NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id"
const val NAV_CONSUMER_APP_ID = "syfoperson"

fun bearerHeader(token: String): String {
    return "Bearer $token"
}

fun callIdArgument(callId: String): StructuredArgument = StructuredArguments.keyValue("callId", callId)
