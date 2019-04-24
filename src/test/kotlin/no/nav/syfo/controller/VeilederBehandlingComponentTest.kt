package no.nav.syfo.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.dao.VeilederBehandlingDAO
import no.nav.syfo.util.TestUtils.loggInnSomVeileder
import no.nav.syfo.util.TestUtils.loggUt
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.inject.Inject


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = [LocalApplication::class])
@AutoConfigureMockMvc
@DirtiesContext
class VeilederBehandlingComponentTest {

    @Inject
    private lateinit var veilederBehandlingDAO: VeilederBehandlingDAO

    @Inject
    private lateinit var mockMvc: MockMvc

    @Inject
    private lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    private val brukerAktorId1 = "aktorId1"
    private val brukerAktorId2 = "aktorId2"
    private val brukerAktorId3 = "aktorId3"

    private val veilederIdent1 = "Z888888"
    private val veilederIdent2 = "Z999999"

    private val enhet1 = "1234"
    private val enhet2 = "2345"

    private val tilknytning1 = VeilederBrukerKnytningNoArgs(veilederIdent1, brukerAktorId1, enhet1)
    private val tilknytning2 = VeilederBrukerKnytningNoArgs(veilederIdent1, brukerAktorId2, enhet1)
    private val tilknytning3 = VeilederBrukerKnytningNoArgs(veilederIdent2, brukerAktorId3, enhet2)

    private val tilknytningListe = listOf(
            tilknytning1,
            tilknytning2,
            tilknytning3
    )

    @Before
    fun setup() {
        loggInnSomVeileder(oidcRequestContextHolder, veilederIdent1)
    }

    @After
    fun cleanUp() {
        fjernBrukere()
        loggUt(oidcRequestContextHolder)
    }

    @Test
    fun sjekkAtVeilederBrukerTilknytningerKanLagresOgHentesRiktig() {
        val jsonLagringsStreng = ObjectMapper().writeValueAsString(tilknytningListe)

        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken("intern").idToken

        mockMvc.perform(MockMvcRequestBuilders.post("/api/veilederbehandling")
                .header("Authorization", "Bearer $idToken")
                .contentType(APPLICATION_JSON)
                .content(jsonLagringsStreng))
                .andExpect(MockMvcResultMatchers.status().isOk)

        val responsFraVeileder1Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/veiledere/$veilederIdent1")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString
        val responsFraVeileder2Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/veiledere/$veilederIdent2")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString

        val typeRef = object : TypeReference<ArrayList<VeilederBrukerKnytningNoArgs>>() {}

        val knytningerPaaVeileder1: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraVeileder1Restkall, typeRef)
        val knytningerPaaVeileder2: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraVeileder2Restkall, typeRef)

        assertThat(knytningerPaaVeileder1).anyMatch { it.equals(tilknytning1) }
        assertThat(knytningerPaaVeileder1).anyMatch { it.equals(tilknytning2) }
        assertThat(knytningerPaaVeileder2).anyMatch { it.equals(tilknytning3) }
    }

    @Test
    fun sjekkAtVeilederBrukerTilknytningerKanLagresOgHentesRiktigPaaEnhet() {
        val jsonLagringsStreng = ObjectMapper().writeValueAsString(tilknytningListe)

        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken("intern").idToken

        mockMvc.perform(MockMvcRequestBuilders.post("/api/veilederbehandling")
                .header("Authorization", "Bearer $idToken")
                .contentType(APPLICATION_JSON)
                .content(jsonLagringsStreng))
                .andExpect(MockMvcResultMatchers.status().isOk)

        val responsFraEnhet1Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/enheter/$enhet1/veiledere")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString
        val responsFraEnhet2Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/enheter/$enhet2/veiledere")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString

        val typeRef = object : TypeReference<ArrayList<VeilederBrukerKnytningNoArgs>>() {}

        val knytningerPaaVeileder1: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraEnhet1Restkall, typeRef)
        val knytningerPaaVeileder2: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraEnhet2Restkall, typeRef)

        assertThat(knytningerPaaVeileder1).anyMatch { it.equals(tilknytning1) }
        assertThat(knytningerPaaVeileder1).anyMatch { it.equals(tilknytning2) }
        assertThat(knytningerPaaVeileder2).anyMatch { it.equals(tilknytning3) }
    }


    private fun fjernBrukere() {
        arrayOf(tilknytning1, tilknytning2, tilknytning3).forEach {
            veilederBehandlingDAO.slettVeilederBrukerKnytning(VeilederBrukerKnytning(it.veilederIdent, it.aktorId, it.enhet))
        }
    }

    private class VeilederBrukerKnytningNoArgs(var veilederIdent: String = "", var aktorId: String = "", var enhet: String = "") {
        fun equals(other: VeilederBrukerKnytningNoArgs): Boolean {
            return veilederIdent == other.veilederIdent && aktorId == other.aktorId && enhet == other.enhet
        }
    }

}
