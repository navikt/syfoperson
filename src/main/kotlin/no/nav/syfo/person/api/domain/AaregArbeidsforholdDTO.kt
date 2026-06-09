package no.nav.syfo.person.api.domain

import no.nav.syfo.client.aareg.ArbeidsforholdResponse
import no.nav.syfo.client.aareg.IdentType
import no.nav.syfo.client.aareg.gjeldendeAnsettelsesdetalj
import no.nav.syfo.domain.PersonIdentNumber
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.collections.map

data class ArbeidsforholdPersonDTO(
    val personident: String,
    val arbeidsforhold: List<ArbeidsforholdDTO>,
) {
    companion object {
        fun fromArbeidsforhold(personident: PersonIdentNumber, allArbeidsforholdResponse: List<ArbeidsforholdResponse>) =
            ArbeidsforholdPersonDTO(
                personident = personident.value,
                arbeidsforhold = allArbeidsforholdResponse.map { arbeidsforhold ->
                    val gjeldendeAnsettelsesdetalj = arbeidsforhold.ansettelsesdetaljer.gjeldendeAnsettelsesdetalj()
                    ArbeidsforholdDTO(
                        navArbeidsforholdId = arbeidsforhold.navArbeidsforholdId,
                        opprettet = arbeidsforhold.opprettet,
                        sistBekreftet = arbeidsforhold.sistBekreftet,
                        orgnummer = arbeidsforhold.arbeidssted.identer
                            .first { it.type == IdentType.ORGANISASJONSNUMMER }
                            .ident,
                        type = arbeidsforhold.type.beskrivelse,
                        ansettelseStart = arbeidsforhold.ansettelsesperiode.startdato,
                        ansettelseSlutt = arbeidsforhold.ansettelsesperiode.sluttdato,
                        ansettelsesform = gjeldendeAnsettelsesdetalj.ansettelsesform?.beskrivelse,
                        yrke = gjeldendeAnsettelsesdetalj.yrke.beskrivelse,
                        stillingsprosent = gjeldendeAnsettelsesdetalj.avtaltStillingsprosent.toInt().toString()
                    )
                }
            )
    }
}

data class ArbeidsforholdDTO(
    val navArbeidsforholdId: Int,
    val opprettet: LocalDateTime,
    val sistBekreftet: LocalDateTime,
    val orgnummer: String,
    val type: String, // "Ordinært arbeidsforhold"
    val ansettelseStart: LocalDate,
    val ansettelseSlutt: LocalDate?,
    val ansettelsesform: String?, // Fast ansettelse
    val yrke: String,
    val stillingsprosent: String,
)
