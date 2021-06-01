package no.nav.syfo.consumer.pdl

import no.nav.syfo.person.api.domain.AktorId

data class PdlIdenterResponse(
    val errors: List<PdlError>?,
    val data: PdlHentIdenter?,
)

data class PdlHentIdenter(
    val hentIdenter: PdlIdenter?,
)

data class PdlIdenter(
    val identer: List<PdlIdent>,
)

data class PdlIdent(
    val ident: String,
    val historisk: Boolean,
    val gruppe: String,
)

enum class IdentType {
    FOLKEREGISTERIDENT,
    NPID,
    AKTORID,
}

fun PdlHentIdenter?.aktorId(): AktorId? {
    val identer = this?.hentIdenter?.identer

    return identer?.firstOrNull {
        it.gruppe == IdentType.AKTORID.name
    }?.ident?.let {
        AktorId(it)
    }
}
