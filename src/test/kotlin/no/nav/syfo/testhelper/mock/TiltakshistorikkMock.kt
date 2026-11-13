package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import java.time.LocalDate
import no.nav.syfo.client.tiltakshistorikk.TiltakshistorikkRequest
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_TILTAK_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_TILTAK_HISTORISK_PERSONIDENT

const val TILTAK_ARENA_AKTIV_ID = "11111111-1111-1111-1111-111111111111"
const val TILTAK_TEAM_KOMET_AKTIV_ID = "22222222-2222-2222-2222-222222222222"
const val TILTAK_TEAM_TILTAK_AKTIV_ID = "33333333-3333-3333-3333-333333333333"
const val TILTAK_HISTORISK_IDENT_AKTIV_ID = "44444444-4444-4444-4444-444444444444"
const val TILTAK_UKJENT_TILTAKSKODE = "NY_TILTAKSKODE"

suspend fun MockRequestHandleScope.tiltakshistorikkMockResponse(request: HttpRequestData): HttpResponseData {
    val identer = request.receiveBody<TiltakshistorikkRequest>().identer
    val today = LocalDate.now()

    if (ARBEIDSTAKER_TILTAK_ERROR.value in identer) {
        return respondError(HttpStatusCode.InternalServerError)
    }

    val historikk = mutableListOf<Map<String, Any?>>()
    if (ARBEIDSTAKER_PERSONIDENT.value in identer) {
        historikk += aktiveOgInaktiveTiltak(ARBEIDSTAKER_PERSONIDENT.value, today)
    }
    if (ARBEIDSTAKER_TILTAK_HISTORISK_PERSONIDENT.value in identer) {
        historikk += teamKometDeltakelse(
            id = TILTAK_HISTORISK_IDENT_AKTIV_ID,
            norskIdent = ARBEIDSTAKER_TILTAK_HISTORISK_PERSONIDENT.value,
            status = "DELTAR",
            tiltakskode = "AVKLARING",
            startDato = today.minusWeeks(1),
            sluttDato = today.plusWeeks(3),
        )
    }

    return respond(mapOf("historikk" to historikk))
}

private fun aktiveOgInaktiveTiltak(norskIdent: String, today: LocalDate): List<Map<String, Any?>> = listOf(
    arenaDeltakelse(
        id = TILTAK_ARENA_AKTIV_ID,
        norskIdent = norskIdent,
        status = "GJENNOMFORES",
        startDato = today.minusMonths(1),
        sluttDato = today,
    ),
    teamKometDeltakelse(
        id = TILTAK_TEAM_KOMET_AKTIV_ID,
        norskIdent = norskIdent,
        status = "DELTAR",
        tiltakskode = TILTAK_UKJENT_TILTAKSKODE,
        startDato = today.minusMonths(1),
        sluttDato = today.plusMonths(2),
    ),
    teamTiltakAvtale(
        id = TILTAK_TEAM_TILTAK_AKTIV_ID,
        norskIdent = norskIdent,
        status = "GJENNOMFORES",
        startDato = today.minusMonths(1),
        sluttDato = null,
    ),
    arenaDeltakelse(
        id = "55555555-5555-5555-5555-555555555555",
        norskIdent = norskIdent,
        status = "FULLFORT",
        startDato = today.minusYears(1),
        sluttDato = today.minusMonths(6),
    ),
    teamKometDeltakelse(
        id = "66666666-6666-6666-6666-666666666666",
        norskIdent = norskIdent,
        status = "AVBRUTT",
        tiltakskode = "OPPFOLGING",
        startDato = today.minusMonths(1),
        sluttDato = today.plusMonths(1),
    ),
    teamKometDeltakelse(
        id = "77777777-7777-7777-7777-777777777777",
        norskIdent = norskIdent,
        status = "VENTER_PA_OPPSTART",
        tiltakskode = "OPPFOLGING",
        startDato = today.plusWeeks(2),
        sluttDato = today.plusMonths(3),
    ),
    teamTiltakAvtale(
        id = "88888888-8888-8888-8888-888888888888",
        norskIdent = norskIdent,
        status = "GJENNOMFORES",
        startDato = today.minusMonths(3),
        sluttDato = today.minusDays(1),
    ),
    teamTiltakAvtale(
        id = "99999999-9999-9999-9999-999999999999",
        norskIdent = norskIdent,
        status = "KLAR_FOR_OPPSTART",
        startDato = today.plusWeeks(1),
        sluttDato = today.plusMonths(3),
    ),
    mapOf(
        "type" to "NyDeltakelse",
        "id" to "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "norskIdent" to norskIdent,
        "tittel" to "Ukjent type",
    ),
    mapOf(
        "type" to "TeamTiltakAvtale",
        "id" to "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
        "norskIdent" to norskIdent,
    ),
)

private fun arenaDeltakelse(
    id: String,
    norskIdent: String,
    status: String,
    startDato: LocalDate?,
    sluttDato: LocalDate?,
) = mapOf(
    "type" to "ArenaDeltakelse",
    "id" to id,
    "norskIdent" to norskIdent,
    "opphav" to "ARENA",
    "startDato" to startDato,
    "sluttDato" to sluttDato,
    "opprettetTidspunkt" to "2026-01-01T10:00:00Z",
    "oppdatertTidspunkt" to "2026-01-01T10:00:00Z",
    "tittel" to "Arbeidsrettet rehabilitering hos Arrangør AS",
    "arenaId" to 123,
    "status" to status,
    "tiltakstype" to mapOf("tiltakskode" to "ARBRRHDAG", "navn" to "Arbeidsrettet rehabilitering"),
    "gjennomforing" to mapOf("id" to id, "navn" to "Fritekst", "deltidsprosent" to 100.0),
    "arrangor" to arrangor(),
    "deltidsprosent" to 100.0,
    "dagerPerUke" to 5.0,
)

private fun teamKometDeltakelse(
    id: String,
    norskIdent: String,
    status: String,
    tiltakskode: String,
    startDato: LocalDate?,
    sluttDato: LocalDate?,
) = mapOf(
    "type" to "TeamKometDeltakelse",
    "id" to id,
    "norskIdent" to norskIdent,
    "opphav" to "TEAM_KOMET",
    "startDato" to startDato,
    "sluttDato" to sluttDato,
    "opprettetTidspunkt" to "2026-01-01T10:00:00Z",
    "oppdatertTidspunkt" to "2026-01-01T10:00:00Z",
    "tittel" to "Oppfølging hos Arrangør AS",
    "status" to mapOf("type" to status, "aarsak" to null, "opprettetTidspunkt" to "2026-01-01T10:00:00"),
    "tiltakstype" to mapOf("tiltakskode" to tiltakskode, "navn" to "Oppfølging"),
    "gjennomforing" to mapOf("id" to id, "navn" to "Fritekst", "deltidsprosent" to null),
    "arrangor" to arrangor(),
    "deltidsprosent" to null,
    "dagerPerUke" to null,
)

private fun teamTiltakAvtale(
    id: String,
    norskIdent: String,
    status: String,
    startDato: LocalDate?,
    sluttDato: LocalDate?,
) = mapOf(
    "type" to "TeamTiltakAvtale",
    "id" to id,
    "norskIdent" to norskIdent,
    "opphav" to "TEAM_TILTAK",
    "startDato" to startDato,
    "sluttDato" to sluttDato,
    "opprettetTidspunkt" to "2026-01-01T10:00:00Z",
    "oppdatertTidspunkt" to "2026-01-01T10:00:00Z",
    "tittel" to "Arbeidstrening hos Arbeidsgiver AS",
    "tiltakstype" to mapOf("tiltakskode" to "ARBEIDSTRENING", "navn" to "Arbeidstrening"),
    "status" to status,
    "stillingsprosent" to 100.0,
    "dagerPerUke" to 5.0,
    "arbeidsgiver" to mapOf("organisasjonsnummer" to "999999999", "navn" to "Arbeidsgiver AS"),
)

private fun arrangor() = mapOf(
    "hovedenhet" to mapOf("organisasjonsnummer" to "888888888", "navn" to "Arrangør AS"),
    "underenhet" to mapOf("organisasjonsnummer" to "999999999", "navn" to "Arrangør AS avd. Oslo"),
)
