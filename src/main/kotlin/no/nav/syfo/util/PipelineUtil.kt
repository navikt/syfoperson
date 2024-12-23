package no.nav.syfo.util

import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.domain.PersonIdentNumber
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo")

fun RoutingContext.getBearerHeader(): String? {
    return this.call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
}

fun ApplicationCall.getCallId(): String {
    return this.request.headers[NAV_CALL_ID_HEADER].toString()
}

fun RoutingContext.getCallId(): String {
    return this.call.getCallId()
}

fun ApplicationCall.getConsumerId(): String {
    return this.request.headers[NAV_CONSUMER_ID_HEADER].toString()
}

fun RoutingContext.getPersonIdent(): String? {
    return this.call.request.headers[NAV_PERSONIDENT_HEADER]
}

suspend fun RoutingContext.personRequestHandler(
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

suspend fun RoutingContext.handleApiError(
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
    val message = "Failed to process request successfully, message=${ex.message}"
    if (responseStatus != HttpStatusCode.Forbidden) {
        log.error("Failed to get response for resource=$resource, status=${responseStatus.value} message=$message callId=$callId", ex)
    }
    call.respond(responseStatus, message)
}

class ForbiddenAccessException(
    message: String = "Denied NAVIdent access to personIdent",
) : RuntimeException(message)
