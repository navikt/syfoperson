package no.nav.syfo.consumer.azuread.v2

import java.io.Serializable
import java.time.LocalDateTime

data class AzureAdV2Token(
    val accessToken: String,
    val expires: LocalDateTime
) : Serializable
