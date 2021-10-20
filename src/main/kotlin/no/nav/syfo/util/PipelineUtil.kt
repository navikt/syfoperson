package no.nav.syfo.util

import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.domain.PersonIdentNumber
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo")

fun PipelineContext<out Unit, ApplicationCall>.getHeader(header: String): String? {
    return this.call.request.headers[header]
}

fun PipelineContext<out Unit, ApplicationCall>.getBearerHeader(): String? {
    return this.call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
}

fun PipelineContext<out Unit, ApplicationCall>.getCallId(): String {
    return this.call.request.headers[NAV_CALL_ID_HEADER].toString()
}

fun PipelineContext<out Unit, ApplicationCall>.getConsumerId(): String {
    return this.call.request.headers[NAV_CONSUMER_ID_HEADER].toString()
}

fun PipelineContext<out Unit, ApplicationCall>.getPersonIdent(): String? {
    return this.call.request.headers[NAV_PERSONIDENT_HEADER]
}

suspend fun PipelineContext<out Unit, ApplicationCall>.personRequestHandler(
    resource: String,
    veilederTilgangskontrollClient: VeilederTilgangskontrollClient,
    requestBlock: suspend () -> Unit,
) {
    try {
        val personIdentNumber = getPersonIdent()?.let { requestedPersonIdent ->
            PersonIdentNumber(requestedPersonIdent)
        } ?: throw IllegalArgumentException("No personIdentNumber supplied in header")

        val token = getBearerHeader()
            ?: throw IllegalArgumentException("No Authorization header supplied")

        val hasAccess = veilederTilgangskontrollClient.hasAccess(
            callId = getCallId(),
            personIdentNumber = personIdentNumber,
            token = token,
        )
        if (hasAccess) {
            requestBlock()
        } else {
            throw ForbiddenAccessException()
        }
    } catch (ex: Exception) {
        handleApiError(
            ex = ex,
            resource = resource,
        )
    }
}

suspend fun PipelineContext<out Unit, ApplicationCall>.handleApiError(
    ex: Exception,
    resource: String,
) {
    val callId = getCallId()
    val responseStatus: HttpStatusCode = when (ex) {
        is ResponseException -> {
            ex.response.status
        }
        is IllegalArgumentException -> {
            HttpStatusCode.BadRequest
        }
        is ForbiddenAccessException -> {
            HttpStatusCode.Forbidden
        }
        else -> {
            HttpStatusCode.InternalServerError
        }
    }
    val message = "Failed to process request successfully, callId=$callId, message=${ex.message}"
    if (responseStatus == HttpStatusCode.Forbidden) {
        log.error("Failed to get response for resource=$resource, status=${responseStatus.value} message=$message callId=$callId")
    }
    call.respond(responseStatus, message)
}

class ForbiddenAccessException(
    message: String = "Denied NAVIdent access to personIdent",
) : RuntimeException(message)
