package no.nav.syfo.client.pdl

data class PdlIdentRequest(
    val query: String,
    val variables: PdlIdentVariables,
)

data class PdlIdentVariables(
    val ident: String,
    val navnHistorikk: Boolean = false,
)
