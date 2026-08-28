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
import no.nav.syfo.client.aap.AapPeriode
import no.nav.syfo.client.aap.AapSak
import no.nav.syfo.client.aap.AapSakerResponse
import no.nav.syfo.client.aap.AapSoknadStatus
import no.nav.syfo.client.aap.AapVedtak
import no.nav.syfo.client.aap.Kilde
import no.nav.syfo.client.azuread.AzureAdToken
import no.nav.syfo.person.api.domain.AapRequest
import no.nav.syfo.person.api.domain.AapSakerDTO
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import no.nav.syfo.testhelper.generateJWT
import no.nav.syfo.testhelper.setupApiAndClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PersonAapSakerTest {
    private val externalMockEnvironment = ExternalMockEnvironment()
    private val url = "$apiPersonBasePath/aap-saker/query"
    private val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
    )
    private val today = LocalDate.of(2026, 8, 24)

    @BeforeEach
    fun beforeEach() {
        every { externalMockEnvironment.valkeyStore.getObject<AzureAdToken>(any()) } returns null
        every { externalMockEnvironment.valkeyStore.getObject<AapSakerResponse>(any()) } returns null
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `returns soknader and vedtak with calculated activity`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(AapRequest(ARBEIDSTAKER_PERSONIDENT.value))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<AapSakerDTO>()
            assertEquals(1, body.soknader.size)
            assertEquals(true, body.soknader.first().erAktiv)
            assertEquals(1, body.vedtak.size)
            assertEquals(true, body.vedtak.first().erAktivt)
        }
    }

    @Test
    fun `returns bad request for invalid personident`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(AapRequest("invalid"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `returns unauthorized without token`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(AapRequest(ARBEIDSTAKER_PERSONIDENT.value))
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `returns forbidden when veileder lacks access`() {
        testApplication {
            val client = setupApiAndClient(externalMockEnvironment)
            val response = client.post(url) {
                bearerAuth(validToken)
                contentType(ContentType.Application.Json)
                setBody(AapRequest(ARBEIDSTAKER_VEILEDER_NO_ACCESS.value))
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `maps active soknad and vedtak`() {
        val response = AapSakerResponse(
            saker = listOf(
                AapSak(
                    sakid = "kelvin-sak-1",
                    statuskode = AapSoknadStatus.SOKNAD_UNDER_BEHANDLING,
                    soknadsdatoer = listOf(today.minusMonths(1)),
                    vedtak = listOf(
                        generateVedtak(
                            vedtaksdato = today.minusMonths(2),
                            fom = today.minusDays(1),
                            tom = null,
                        )
                    ),
                    kilde = Kilde.KELVIN,
                )
            )
        )

        val result = AapSakerDTO.fromSaker(response, today)

        assertEquals(1, result.soknader.size)
        assertEquals("kelvin-sak-1", result.soknader.first().sakid)
        assertEquals(listOf(today.minusMonths(1)), result.soknader.first().soknadsdatoer)
        assertEquals(AapSoknadStatus.SOKNAD_UNDER_BEHANDLING, result.soknader.first().statuskode)
        assertTrue(result.soknader.first().erAktiv)

        assertEquals(1, result.vedtak.size)
        assertEquals("kelvin-sak-1", result.vedtak.first().sakid)
        assertEquals(Kilde.KELVIN, result.vedtak.first().kilde)
        assertEquals(today.minusMonths(2), result.vedtak.first().vedtaksdato)
        assertEquals(today.minusDays(1), result.vedtak.first().perioder.first().fraOgMedDato)
        assertEquals(null, result.vedtak.first().perioder.first().tilOgMedDato)
        assertTrue(result.vedtak.first().erAktivt)
    }

    @Test
    fun `maps soknad from Kelvin and vedtak from both sources`() {
        val response = AapSakerResponse(
            saker = listOf(
                AapSak(
                    sakid = "arena-sak-1",
                    statuskode = AapSoknadStatus.AVSLU,
                    soknadsdatoer = emptyList(),
                    vedtak = listOf(
                        generateVedtak(
                            vedtaksdato = today.minusMonths(6),
                            fom = today.plusDays(1),
                            tom = today.plusMonths(1),
                        )
                    ),
                    kilde = Kilde.ARENA,
                ),
                AapSak(
                    sakid = "kelvin-sak-1",
                    statuskode = AapSoknadStatus.FERDIGBEHANDLET,
                    soknadsdatoer = listOf(today.minusMonths(6).minusDays(1)),
                    vedtak = emptyList(),
                    kilde = Kilde.KELVIN,
                ),
            )
        )

        val result = AapSakerDTO.fromSaker(response, today)

        assertEquals(1, result.soknader.size)
        assertEquals("kelvin-sak-1", result.soknader.first().sakid)
        assertFalse(result.soknader.first().erAktiv)
        assertEquals(1, result.vedtak.size)
        assertEquals("arena-sak-1", result.vedtak.first().sakid)
        assertEquals(Kilde.ARENA, result.vedtak.first().kilde)
        assertFalse(result.vedtak.first().erAktivt)
    }

    @Test
    fun `does not treat period without start date as active`() {
        val response = AapSakerResponse(
            saker = listOf(
                AapSak(
                    sakid = "arena-sak-1",
                    statuskode = AapSoknadStatus.IVERK,
                    soknadsdatoer = emptyList(),
                    vedtak = listOf(
                        generateVedtak(
                            vedtaksdato = today.minusYears(1),
                            fom = null,
                            tom = today.plusMonths(1),
                        )
                    ),
                    kilde = Kilde.ARENA,
                )
            )
        )

        assertFalse(AapSakerDTO.fromSaker(response, today).vedtak.first().erAktivt)
    }

    private fun generateVedtak(
        vedtaksdato: LocalDate,
        fom: LocalDate?,
        tom: LocalDate?,
    ) = AapVedtak(
        vedtaksdato = vedtaksdato,
        perioder = listOf(AapPeriode(fom, tom)),
    )
}
