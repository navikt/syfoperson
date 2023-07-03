package no.nav.syfo.person.api.domain.syfomodiaperson

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TilrettelagtKommunikasjon.TalesprakTolk::class, name = "talesprakTolk"),
    JsonSubTypes.Type(value = TilrettelagtKommunikasjon.TegnsprakTolk::class, name = "tegnsprakTolk")
)

sealed class TilrettelagtKommunikasjon {
    data class TalesprakTolk(val sprak: String) : TilrettelagtKommunikasjon()
    data class TegnsprakTolk(val sprak: String) : TilrettelagtKommunikasjon()
}
