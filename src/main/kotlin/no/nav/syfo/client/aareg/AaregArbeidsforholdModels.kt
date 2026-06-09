package no.nav.syfo.client.aareg

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

// Se https://aareg-services.intern.dev.nav.no/swagger-ui/index.html?urls.primaryName=aareg.api.v2#/arbeidstaker/finnArbeidsforholdPrArbeidstaker_1
// Og tråd i slack: https://nav-it.slack.com/archives/CAY78MSH5/p1779776100722439
data class ArbeidsforholdRequest(
    val arbeidstakerId: String,
)

// Sitat: "Dersom noen har flere yrker samtidig på samme underenhet vil de komme i 2 forskjellige arbeidsforhold. Det er fullt lovlig å bytte yrke underveis på samme arbeidsforhold"
data class ArbeidsforholdResponse(
    val navArbeidsforholdId: Int,
    val opprettet: LocalDateTime,
    val sistBekreftet: LocalDateTime,
    val type: Kode,
    val arbeidssted: Arbeidssted,
    val ansettelsesperiode: Ansettelsesperiode,
    val ansettelsesdetaljer: List<Ansettelsesdetalj>,
)

data class Kode(
    val kode: String,
    val beskrivelse: String,
)

data class Arbeidssted(
    val type: ArbeidsstedType,
    val identer: List<Ident>,
)

data class Ident(
    val type: IdentType,
    val ident: String,
    val gjeldende: Boolean? = null,
)

enum class ArbeidsstedType {
    Hovedenhet,
    Underenhet,
    Person,
}

enum class IdentType {
    AKTORID,
    FOLKEREGISTERIDENT,
    ORGANISASJONSNUMMER,
}

data class Ansettelsesperiode(
    val startdato: LocalDate,
    val sluttdato: LocalDate? = null,
)

// "På bakrommet har vi 1 ansettelsesdetalj pr måned - disse squashes på vei ut slik at dere får en liste hvor de er gyldig fra/til - hvor den siste er gyldig til null"
data class Ansettelsesdetalj(
    val ansettelsesform: Kode?,
    val yrke: Kode,
    val antallTimerPrUke: Double,
    val avtaltStillingsprosent: Double,
    val rapporteringsmaaneder: FraTil,
)

data class FraTil(
    val fra: YearMonth,
    val til: YearMonth?,
)

fun List<Ansettelsesdetalj>.gjeldendeAnsettelsesdetalj(): Ansettelsesdetalj =
    this.firstOrNull { it.rapporteringsmaaneder.til == null }
        ?: maxByOrNull { it.rapporteringsmaaneder.fra }
        ?: throw IllegalStateException("Fant ikke gjeldende ansettelsesdetalj")
