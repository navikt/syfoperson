package no.nav.syfo.client.kodeverk

data class KodeverkBetydninger(
    val betydninger: Map<String, List<Betydning>>,
) {
    fun toPostInformasjonListe(): List<Postinformasjon> {
        return betydninger.map {
            Postinformasjon(
                postnummer = it.key,
                poststed = it.value.first().beskrivelser["nb"]?.term,
            )
        }
    }
}

data class Betydning(
    val gyldigTil: String, // Trengs ikke
    val gyldigFra: String, // Trengs ikke
    val beskrivelser: Map<String, Beskrivelse>,
)

data class Beskrivelse(
    val term: String,
    val tekst: String, // Trengs ikke
)
