package no.nav.syfo.person.api

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.mockk.clearAllMocks
import io.mockk.every
import java.time.LocalDate
import no.nav.syfo.client.azuread.AzureAdToken
import no.nav.syfo.client.pdl.PdlHentPerson
import no.nav.syfo.client.tiltakshistorikk.TeamKometStatus
import no.nav.syfo.client.tiltakshistorikk.TiltakshistorikkDeltakelse
import no.nav.syfo.client.tiltakshistorikk.TiltakshistorikkResponse
import no.nav.syfo.client.tiltakshistorikk.Tiltakstype
import no.nav.syfo.person.api.domain.TiltakRequestDTO
import no.nav.syfo.person.api.domain.TiltakResponseDTO
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PDL_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_TILTAK_ERROR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_TILTAK_MED_HISTORISK_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.testhelper.generateJWT
import no.nav.syfo.testhelper.mock.TILTAK_ARENA_AKTIV_ID
import no.nav.syfo.testhelper.mock.TILTAK_HISTORISK_IDENT_AKTIV_ID
import no.nav.syfo.testhelper.mock.TILTAK_TEAM_KOMET_AKTIV_ID
import no.nav.syfo.testhelper.mock.TILTAK_TEAM_TILTAK_AKTIV_ID
import no.nav.syfo.testhelper.mock.TILTAK_UKJENT_TILTAKSKODE
import no.nav.syfo.testhelper.setupApiAndClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PersonTiltakTest {
    private val externalMockEnvironment = ExternalMockEnvironment()
    private val url = "$apiPersonBasePath/tiltak/query"
    private val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
    )
    private val today = LocalDate.of(2026, 11, 12)

    @BeforeEach
    fun beforeEach() {
        every { externalMockEnvironment.valkeyStore.getObject<AzureAdToken>(any()) } returns null
        every { externalMockEnvironment.valkeyStore.getObject<PdlHentPerson>(any()) } returns null
        every { externalMockEnvironment.valkeyStore.getObject<TiltakshistorikkResponse>(any()) } returns null
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `returns active tiltak from all sources`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(TiltakRequestDTO(ARBEIDSTAKER_PERSONIDENT.value))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<TiltakResponseDTO>()
            assertEquals(
                setOf(TILTAK_ARENA_AKTIV_ID, TILTAK_TEAM_KOMET_AKTIV_ID, TILTAK_TEAM_TILTAK_AKTIV_ID),
                body.tiltak.map { it.id }.toSet(),
            )
            val teamKomet = body.tiltak.first { it.id == TILTAK_TEAM_KOMET_AKTIV_ID }
            assertEquals(TILTAK_UKJENT_TILTAKSKODE, teamKomet.tiltakskode)
            assertEquals("Oppfølging", teamKomet.tiltakstypeNavn)
            assertNull(body.tiltak.first { it.id == TILTAK_TEAM_TILTAK_AKTIV_ID }.sluttDato)
        }
    }

    @Test
    fun `includes tiltak registered on historisk personident`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(TiltakRequestDTO(ARBEIDSTAKER_TILTAK_MED_HISTORISK_PERSONIDENT.value))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<TiltakResponseDTO>()
            assertEquals(listOf(TILTAK_HISTORISK_IDENT_AKTIV_ID), body.tiltak.map { it.id })
        }
    }

    @Test
    fun `returns empty list when person has no tiltak`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(TiltakRequestDTO(ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.body<TiltakResponseDTO>().tiltak.isEmpty())
        }
    }

    @Test
    fun `returns forbidden when veileder lacks access`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(TiltakRequestDTO(ARBEIDSTAKER_VEILEDER_NO_ACCESS.value))
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `returns unauthorized without token`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(TiltakRequestDTO(ARBEIDSTAKER_PERSONIDENT.value))
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `returns bad request for invalid personident`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(TiltakRequestDTO("invalid"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `returns error when person is not found in PDL`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(TiltakRequestDTO(ARBEIDSTAKER_PDL_ERROR.value))
            }

            assertEquals(HttpStatusCode.InternalServerError, response.status)
        }
    }

    @Test
    fun `returns error when tiltakshistorikk fails`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(TiltakRequestDTO(ARBEIDSTAKER_TILTAK_ERROR.value))
            }

            assertEquals(HttpStatusCode.InternalServerError, response.status)
        }
    }

    @Test
    fun `treats tiltak with active status and sluttDato today or later as active`() {
        val response = TiltakshistorikkResponse(
            historikk = listOf(
                arenaDeltakelse(id = "arena-idag", status = "GJENNOMFORES", sluttDato = today),
                teamKometDeltakelse(id = "komet-uten-slutt", status = "DELTAR", sluttDato = null),
                teamTiltakAvtale(id = "tiltak-fremtid", status = "GJENNOMFORES", sluttDato = today.plusDays(1)),
            )
        )

        val result = TiltakResponseDTO.fromHistorikk(response, today)

        assertEquals(listOf("arena-idag", "komet-uten-slutt", "tiltak-fremtid"), result.tiltak.map { it.id })
    }

    @Test
    fun `filters out avsluttet, avbrutt, ikke startet and passed sluttDato`() {
        val response = TiltakshistorikkResponse(
            historikk = listOf(
                arenaDeltakelse(id = "arena-fullfort", status = "FULLFORT", sluttDato = today.minusMonths(1)),
                arenaDeltakelse(id = "arena-tilbud", status = "TILBUD", sluttDato = today.plusMonths(1)),
                teamKometDeltakelse(id = "komet-avbrutt", status = "AVBRUTT", sluttDato = today.plusMonths(1)),
                teamKometDeltakelse(id = "komet-venter", status = "VENTER_PA_OPPSTART", sluttDato = null),
                teamTiltakAvtale(id = "tiltak-klar", status = "KLAR_FOR_OPPSTART", sluttDato = today.plusMonths(1)),
                teamTiltakAvtale(id = "tiltak-passert", status = "GJENNOMFORES", sluttDato = today.minusDays(1)),
            )
        )

        assertTrue(TiltakResponseDTO.fromHistorikk(response, today).tiltak.isEmpty())
    }

    @Test
    fun `removes duplicate tiltak with same id`() {
        val deltakelse = teamTiltakAvtale(id = "tiltak-1", status = "GJENNOMFORES", sluttDato = null)
        val response = TiltakshistorikkResponse(historikk = listOf(deltakelse, deltakelse))

        assertEquals(1, TiltakResponseDTO.fromHistorikk(response, today).tiltak.size)
    }

    private fun arenaDeltakelse(id: String, status: String, sluttDato: LocalDate?) =
        TiltakshistorikkDeltakelse.ArenaDeltakelse(
            id = id,
            tiltakstype = Tiltakstype(tiltakskode = "ARBRRHDAG", navn = "Arbeidsrettet rehabilitering"),
            startDato = today.minusMonths(1),
            sluttDato = sluttDato,
            status = status,
        )

    private fun teamKometDeltakelse(id: String, status: String, sluttDato: LocalDate?) =
        TiltakshistorikkDeltakelse.TeamKometDeltakelse(
            id = id,
            tiltakstype = Tiltakstype(tiltakskode = "OPPFOLGING", navn = "Oppfølging"),
            startDato = today.minusMonths(1),
            sluttDato = sluttDato,
            status = TeamKometStatus(type = status),
        )

    private fun teamTiltakAvtale(id: String, status: String, sluttDato: LocalDate?) =
        TiltakshistorikkDeltakelse.TeamTiltakAvtale(
            id = id,
            tiltakstype = Tiltakstype(tiltakskode = "ARBEIDSTRENING", navn = "Arbeidstrening"),
            startDato = today.minusMonths(1),
            sluttDato = sluttDato,
            status = status,
        )
}
