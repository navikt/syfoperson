package no.nav.syfo.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.controller.domain.VeilederBrukerKnytning
import no.nav.syfo.repository.dao.VeilederBehandlingDAO
import no.nav.syfo.util.OIDCIssuer.AZURE
import no.nav.syfo.util.TestUtils.loggInnSomVeileder
import no.nav.syfo.util.TestUtils.loggUt
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
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

    private val lagretVeilederIdent1 = "Z999999"
    private val lagretVeilederIdent2 = "Z888888"

    private val lagretAktorId1 = "1234567890123"
    private val lagretAktorId2 = "1234567890124"
    private val lagretAktorId3 = "1234567890125"

    private val lagretEnhet1 = "1234"
    private val lagretEnhet2 = "2345"

    private val tilknytningListe = listOf(
            VeilederBrukerKnytningNoArgs("ident1", "aktorId1", "XXXX"),
            VeilederBrukerKnytningNoArgs("ident2", "aktorId2", "YYYY"),
            VeilederBrukerKnytningNoArgs("ident3", "aktorId3", "ZZZZ")
    )

    @Before
    fun setup() {
        loggInnSomVeileder(oidcRequestContextHolder, lagretVeilederIdent1)
    }

    @After
    fun cleanUp() {
        fjernBrukere()
        loggUt(oidcRequestContextHolder)
    }

    @Test
    fun sjekkAtVeilederBrukerTilknytningerKanLagresRiktig() {
        val jsonLagringsStreng = ObjectMapper().writeValueAsString(tilknytningListe)

        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(AZURE).idToken

        mockMvc.perform(MockMvcRequestBuilders.post("/api/veilederbehandling/registrer")
                .header("Authorization", "Bearer $idToken")
                .contentType(APPLICATION_JSON)
                .content(jsonLagringsStreng))
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun sjekkAtVeilederBrukerTilknytningerKanLagresOgHentesRiktig() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(AZURE).idToken

        val responsFraVeileder1Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/veiledere/$lagretVeilederIdent1")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString
        val responsFraVeileder2Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/veiledere/$lagretVeilederIdent2")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString


        val typeRef = object : TypeReference<ArrayList<VeilederBrukerKnytningNoArgs>>() {}
        val knytningerPaaVeileder1: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraVeileder1Restkall, typeRef)
        val knytningerPaaVeileder2: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraVeileder2Restkall, typeRef)

        assertThat(knytningerPaaVeileder1).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent1, lagretAktorId1, lagretEnhet1)) }
        assertThat(knytningerPaaVeileder2).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent2, lagretAktorId2, lagretEnhet1)) }
        assertThat(knytningerPaaVeileder2).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent2, lagretAktorId3, lagretEnhet2)) }
    }

    @Test
    fun sjekkAtVeilederBrukerTilknytningerKanLagresOgHentesRiktigPaaEnhet() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(AZURE).idToken

        val responsFraEnhet1Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/enheter/$lagretEnhet1/veiledere")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString
        val responsFraEnhet2Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/enheter/$lagretEnhet2/veiledere")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString

        val typeRef = object : TypeReference<ArrayList<VeilederBrukerKnytningNoArgs>>() {}
        val knytningerPaaEnhet1: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraEnhet1Restkall, typeRef)
        val knytningerPaaEnhet2: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraEnhet2Restkall, typeRef)

        assertThat(knytningerPaaEnhet1).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent1, lagretAktorId1, lagretEnhet1)) }
        assertThat(knytningerPaaEnhet1).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent2, lagretAktorId2, lagretEnhet1)) }
        assertThat(knytningerPaaEnhet2).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent2, lagretAktorId3, lagretEnhet2)) }
    }


    private fun fjernBrukere() {
        tilknytningListe.forEach {
            veilederBehandlingDAO.slettVeilederBrukerKnytning(VeilederBrukerKnytning(it.veilederIdent, it.aktorId, it.enhet))
        }
    }

    private class VeilederBrukerKnytningNoArgs(var veilederIdent: String = "", var aktorId: String = "", var enhet: String = "") {
        fun equals(other: VeilederBrukerKnytningNoArgs): Boolean {
            return veilederIdent == other.veilederIdent && aktorId == other.aktorId && enhet == other.enhet
        }
    }

}
