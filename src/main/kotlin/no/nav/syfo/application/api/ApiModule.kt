package no.nav.syfo.application.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.authentication.JwtIssuer
import no.nav.syfo.application.api.authentication.JwtIssuerType
import no.nav.syfo.application.api.authentication.installJwtAuthentication
import no.nav.syfo.application.metric.api.registerMetricApi
import no.nav.syfo.client.kodeverk.KodeverkClient
import no.nav.syfo.client.krr.KRRClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.client.wellknown.WellKnown
import no.nav.syfo.person.api.registrerPersonApi
import no.nav.syfo.person.skjermingskode.SkjermingskodeService

fun Application.apiModule(
    applicationState: ApplicationState,
    environment: Environment,
    wellKnownInternalAzureAD: WellKnown,
    krrClient: KRRClient,
    pdlClient: PdlClient,
    skjermedePersonerPipClient: SkjermedePersonerPipClient,
    kodeverkClient: KodeverkClient,
    veilederTilgangskontrollClient: VeilederTilgangskontrollClient,
) {
    installMetrics()
    installCallId()
    installContentNegotiation()
    installJwtAuthentication(
        jwtIssuerList = listOf(
            JwtIssuer(
                acceptedAudienceList = listOf(environment.azureAppClientId),
                jwtIssuerType = JwtIssuerType.INTERNAL_AZUREAD,
                wellKnown = wellKnownInternalAzureAD,
            ),
        ),
    )
    installStatusPages()

    val skjermingskodeService = SkjermingskodeService(
        skjermedePersonerPipClient = skjermedePersonerPipClient,
        pdlClient = pdlClient,
    )

    routing {
        registerPodApi(
            applicationState = applicationState,
        )
        registerMetricApi()
        authenticate(JwtIssuerType.INTERNAL_AZUREAD.name) {
            registrerPersonApi(
                krrClient = krrClient,
                pdlClient = pdlClient,
                skjermingskodeService = skjermingskodeService,
                skjermedePersonerPipClient = skjermedePersonerPipClient,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
                kodeverkClient = kodeverkClient,
            )
        }
    }
}
