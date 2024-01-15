package no.nav.syfo.client.kodeverk

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import java.time.LocalDate

class KodeverkClient(
    private val redisStore: RedisStore,
    private val baseUrl: String,
) {
    private val httpClient = httpClientDefault()

    suspend fun getPostinformasjon(
        callId: String,
    ): List<Postinformasjon> {
        val cachedValue = redisStore.getListObject<Postinformasjon>(CACHE_POSTINFORMASJON_KEY)

        if (!cachedValue.isNullOrEmpty()) {
            return cachedValue
        } else {
            val postinformasjon = getPostnummerKodeverk(
                callId = callId,
            )
            if (postinformasjon.isNotEmpty()) {
                redisStore.setObject(
                    expireSeconds = CACHE_KODEVERK_POSTINFORMASJON_EXPIRE_SECONDS,
                    key = CACHE_POSTINFORMASJON_KEY,
                    value = postinformasjon,
                )
            }

            return postinformasjon
        }
    }

    private suspend fun getPostnummerKodeverk(
        callId: String,
    ): List<Postinformasjon> {
        val postnummerUrl = "$baseUrl$KODEVERK_POSTNUMMER_BETYDNINGER_PATH"
        return try {
            val response: HttpResponse = httpClient.get(postnummerUrl) {
                parameter(KODEVERK_EKSKLUDER_UGYLDIGE_PARAM, true)
                parameter(KODEVERK_OPPSLAGSDATO_PARAM, "${LocalDate.now()}")
                parameter(KODEVERK_SPRAK_PARAM, "nb")
                header(NAV_CALL_ID_HEADER, callId)
                header(NAV_CONSUMER_ID_HEADER, "srvsyfoperson")
                accept(ContentType.Application.Json)
            }
            COUNT_CALL_KODEVERK_POSTNUMMER_SUCCESS.increment()
            val body = response.body<KodeverkBetydninger>()
            body.toPostInformasjonListe()
        } catch (e: ClientRequestException) {
            handleUnexpectedResponseException(e.response, callId)
            emptyList()
        } catch (e: ServerResponseException) {
            handleUnexpectedResponseException(e.response, callId)
            emptyList()
        } catch (e: ClosedReceiveChannelException) {
            log.error("ClosedReceiveChannelException while requesting Postnummer from kodeverk", e)
            COUNT_CALL_KODEVERK_POSTNUMMER_FAIL.increment()
            emptyList()
        }
    }

    private fun handleUnexpectedResponseException(
        response: HttpResponse,
        callId: String,
    ) {
        log.error(
            "Error while requesting access to Postnummer from kodeverk with {}, {}",
            StructuredArguments.keyValue("statusCode", response.status.value.toString()),
            callIdArgument(callId)
        )
        COUNT_CALL_KODEVERK_POSTNUMMER_FAIL.increment()
    }

    companion object {
        private val log = LoggerFactory.getLogger(KodeverkClient::class.java)

        const val CACHE_POSTINFORMASJON_KEY = "postinformasjon"
        const val CACHE_KODEVERK_POSTINFORMASJON_EXPIRE_SECONDS = 60 * 60 * 24L

        private const val KODEVERK_COMMON_PATH = "/api/v1/kodeverk"
        const val KODEVERK_POSTNUMMER_BETYDNINGER_PATH = "$KODEVERK_COMMON_PATH/Postnummer/koder/betydninger"
        private const val KODEVERK_EKSKLUDER_UGYLDIGE_PARAM = "ekskluderUgyldige"
        private const val KODEVERK_OPPSLAGSDATO_PARAM = "oppslagsdato"
        private const val KODEVERK_SPRAK_PARAM = "spraak"
    }
}
