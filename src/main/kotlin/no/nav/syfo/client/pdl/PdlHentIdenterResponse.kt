package no.nav.syfo.client.pdl

import no.nav.syfo.domain.AktorId

data class PdlHentIdenterResponse(
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
