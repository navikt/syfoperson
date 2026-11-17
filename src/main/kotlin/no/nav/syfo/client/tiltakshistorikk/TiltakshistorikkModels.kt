package no.nav.syfo.client.tiltakshistorikk

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate

data class TiltakshistorikkRequest(
    val identer: List<String>,
)

data class TiltakshistorikkRawResponse(
    val historikk: List<JsonNode>,
)

data class TiltakshistorikkResponse(
    val historikk: List<TiltakshistorikkDeltakelse>,
)

/**
 * Subset av TiltakshistorikkV1Dto fra mulighetsrommet-tiltakshistorikk.
 * Status og tiltakskode leses som String, siden nye verdier kan komme uten ny versjon av API-et.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TiltakshistorikkDeltakelse.ArenaDeltakelse::class, name = "ArenaDeltakelse"),
    JsonSubTypes.Type(value = TiltakshistorikkDeltakelse.TeamKometDeltakelse::class, name = "TeamKometDeltakelse"),
    JsonSubTypes.Type(value = TiltakshistorikkDeltakelse.TeamTiltakAvtale::class, name = "TeamTiltakAvtale"),
)
sealed interface TiltakshistorikkDeltakelse {
    val id: String
    val tiltakstype: Tiltakstype
    val startDato: LocalDate?
    val sluttDato: LocalDate?

    data class ArenaDeltakelse(
        override val id: String,
        override val tiltakstype: Tiltakstype,
        override val startDato: LocalDate?,
        override val sluttDato: LocalDate?,
        val status: String,
    ) : TiltakshistorikkDeltakelse

    data class TeamKometDeltakelse(
        override val id: String,
        override val tiltakstype: Tiltakstype,
        override val startDato: LocalDate?,
        override val sluttDato: LocalDate?,
        val status: TeamKometStatus,
    ) : TiltakshistorikkDeltakelse

    data class TeamTiltakAvtale(
        override val id: String,
        override val tiltakstype: Tiltakstype,
        override val startDato: LocalDate?,
        override val sluttDato: LocalDate?,
        val status: String,
    ) : TiltakshistorikkDeltakelse
}

data class Tiltakstype(
    val tiltakskode: String,
    val navn: String,
)

data class TeamKometStatus(
    val type: String,
)
