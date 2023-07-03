package no.nav.syfo.person.api.domain.syfomodiaperson

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
sealed class TilrettelagtKommunikasjon {
    abstract val sprak: String
    data class TalesprakTolk(override val sprak: String) : TilrettelagtKommunikasjon()
    data class TegnsprakTolk(override val sprak: String) : TilrettelagtKommunikasjon()
}
