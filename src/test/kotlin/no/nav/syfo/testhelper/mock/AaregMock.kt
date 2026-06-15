package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.client.aareg.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AAREG_NOT_FOUND
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AAREG_PERSON_ARBEIDSSTED
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AAREG_SEVERAL_ARBEIDSFORHOLD
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

suspend fun MockRequestHandleScope.aaregMockResponse(request: HttpRequestData): HttpResponseData {
    val personident = request.receiveBody<ArbeidsforholdRequest>().arbeidstakerId

    return when (personident) {
        ARBEIDSTAKER_PERSONIDENT.value -> respond(listOf(generateArbeidsforholdResponse()))
        ARBEIDSTAKER_ALTERNATIVE_PERSONIDENT.value -> respond(listOf(generateArbeidsforholdSeveralYrkerResponse()))
        ARBEIDSTAKER_AAREG_SEVERAL_ARBEIDSFORHOLD.value -> respond(
            listOf(
                generateArbeidsforholdResponse(),
                generateArbeidsforholdSeveralYrkerResponse(),
            )
        )
        ARBEIDSTAKER_AAREG_PERSON_ARBEIDSSTED.value -> respond(
            listOf(
                generateArbeidsforholdResponse(),
                generateArbeidsforholdPersonArbeidsstedResponse(),
            )
        )
        ARBEIDSTAKER_AAREG_NOT_FOUND.value -> respondError(HttpStatusCode.NotFound)
        else -> respond(emptyList<ArbeidsforholdResponse>())
    }
}

fun generateArbeidsforholdResponse(): ArbeidsforholdResponse =
    ArbeidsforholdResponse(
        navArbeidsforholdId = 1234567,
        opprettet = LocalDateTime.of(2020, 1, 1, 10, 0, 0),
        sistBekreftet = LocalDateTime.of(2024, 1, 1, 10, 0, 0),
        type = Kode(
            kode = "ordinaertArbeidsforhold",
            beskrivelse = "Ordinært arbeidsforhold",
        ),
        arbeidssted = Arbeidssted(
            type = ArbeidsstedType.Underenhet,
            identer = listOf(
                Ident(type = IdentType.ORGANISASJONSNUMMER, ident = "912345678")
            )
        ),
        ansettelsesperiode = Ansettelsesperiode(
            startdato = LocalDate.of(2020, 1, 1),
        ),
        ansettelsesdetaljer = listOf(
            Ansettelsesdetalj(
                ansettelsesform = Kode(
                    kode = "fast",
                    beskrivelse = "Fast ansettelse",
                ),
                yrke = Kode(
                    kode = "1234",
                    beskrivelse = "Utvikler",
                ),
                antallTimerPrUke = 37.5,
                avtaltStillingsprosent = 100.0,
                rapporteringsmaaneder = FraTil(
                    fra = YearMonth.of(2020, 1),
                    til = null,
                )
            ),
        ),
    )

private fun generateArbeidsforholdSeveralYrkerResponse(): ArbeidsforholdResponse =
    ArbeidsforholdResponse(
        navArbeidsforholdId = 1234567,
        opprettet = LocalDateTime.of(2020, 1, 1, 10, 0, 0),
        sistBekreftet = LocalDateTime.of(2024, 1, 1, 10, 0, 0),
        type = Kode(
            kode = "ordinaertArbeidsforhold",
            beskrivelse = "Ordinært arbeidsforhold",
        ),
        arbeidssted = Arbeidssted(
            type = ArbeidsstedType.Underenhet,
            identer = listOf(
                Ident(type = IdentType.ORGANISASJONSNUMMER, ident = "912345699")
            )
        ),
        ansettelsesperiode = Ansettelsesperiode(
            startdato = LocalDate.of(2020, 1, 1),
        ),
        ansettelsesdetaljer = listOf(
            Ansettelsesdetalj(
                ansettelsesform = Kode(
                    kode = "fast",
                    beskrivelse = "Fast ansettelse",
                ),
                yrke = Kode(
                    kode = "1230",
                    beskrivelse = "Barnehagelærer",
                ),
                antallTimerPrUke = 40.0,
                avtaltStillingsprosent = 50.0,
                rapporteringsmaaneder = FraTil(
                    fra = YearMonth.of(2010, 1),
                    til = YearMonth.of(2020, 1),
                )
            ),
            Ansettelsesdetalj(
                ansettelsesform = Kode(
                    kode = "fast",
                    beskrivelse = "Fast ansettelse",
                ),
                yrke = Kode(
                    kode = "1233",
                    beskrivelse = "AKS-lærer",
                ),
                antallTimerPrUke = 40.0,
                avtaltStillingsprosent = 100.0,
                rapporteringsmaaneder = FraTil(
                    fra = YearMonth.of(2020, 1),
                    til = null,
                )
            ),
        ),
    )

fun generateArbeidsforholdPersonArbeidsstedResponse(): ArbeidsforholdResponse =
    ArbeidsforholdResponse(
        navArbeidsforholdId = 9999999,
        opprettet = LocalDateTime.of(2021, 1, 1, 10, 0, 0),
        sistBekreftet = LocalDateTime.of(2024, 1, 1, 10, 0, 0),
        type = Kode(
            kode = "ordinaertArbeidsforhold",
            beskrivelse = "Ordinært arbeidsforhold",
        ),
        arbeidssted = Arbeidssted(
            type = ArbeidsstedType.Person,
            identer = listOf(
                Ident(type = IdentType.FOLKEREGISTERIDENT, ident = "99999999999")
            )
        ),
        ansettelsesperiode = Ansettelsesperiode(
            startdato = LocalDate.of(2021, 1, 1),
        ),
        ansettelsesdetaljer = listOf(
            Ansettelsesdetalj(
                ansettelsesform = Kode(
                    kode = "fast",
                    beskrivelse = "Fast ansettelse",
                ),
                yrke = Kode(
                    kode = "5000",
                    beskrivelse = "Hjemmearbeid",
                ),
                antallTimerPrUke = 37.5,
                avtaltStillingsprosent = 100.0,
                rapporteringsmaaneder = FraTil(
                    fra = YearMonth.of(2021, 1),
                    til = null,
                )
            ),
        ),
    )
