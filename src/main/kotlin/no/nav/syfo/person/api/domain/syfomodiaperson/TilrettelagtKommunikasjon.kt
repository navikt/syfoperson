package no.nav.syfo.person.api.domain.syfomodiaperson

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "type")
sealed class TilrettelagtKommunikasjon {
    abstract val sprak: String

    @JsonTypeName(value = "talesprakTolk")
    data class TalesprakTolk(override val sprak: String) : TilrettelagtKommunikasjon()

    @JsonTypeName(value = "tegnsprakTolk")
    data class TegnsprakTolk(override val sprak: String) : TilrettelagtKommunikasjon()
}
