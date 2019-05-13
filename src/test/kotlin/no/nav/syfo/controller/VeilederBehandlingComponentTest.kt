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

    private val lagretFnr1 = "12345678901"
    private val lagretFnr2 = "12345678902"
    private val lagretFnr3 = "12345678903"

    private val lagretEnhet1 = "1234"
    private val lagretEnhet2 = "2345"

    private val tilknytningListe = listOf(
            VeilederBrukerKnytningNoArgs("ident1", "fnr1", "XXXX"),
            VeilederBrukerKnytningNoArgs("ident2", "fnr2", "YYYY"),
            VeilederBrukerKnytningNoArgs("ident3", "fnr3", "ZZZZ")
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
    fun `Sjekk at VeilederBrukerTilknytning-er kan lagres riktig`() {
        val jsonLagringsStreng = ObjectMapper().writeValueAsString(tilknytningListe)

        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(AZURE).idToken

        mockMvc.perform(MockMvcRequestBuilders.post("/api/veilederbehandling/registrer")
                .header("Authorization", "Bearer $idToken")
                .contentType(APPLICATION_JSON)
                .content(jsonLagringsStreng))
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `Sjekk at VeilederBrukerTilknytning-er kan lagres og hentes riktig`() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(AZURE).idToken

        val responsFraVeileder1Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/veiledere/$lagretVeilederIdent1")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString
        val responsFraVeileder2Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/veiledere/$lagretVeilederIdent2")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString


        val typeRef = object : TypeReference<ArrayList<VeilederBrukerKnytningNoArgs>>() {}
        val knytningerPaVeileder1: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraVeileder1Restkall, typeRef)
        val knytningerPaVeileder2: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraVeileder2Restkall, typeRef)

        assertThat(knytningerPaVeileder1).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent1, lagretFnr1, lagretEnhet1)) }
        assertThat(knytningerPaVeileder2).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent2, lagretFnr2, lagretEnhet1)) }
        assertThat(knytningerPaVeileder2).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent2, lagretFnr3, lagretEnhet2)) }
    }

    @Test
    fun `Sjekk at VeilederBrukerTilknytning-er kan lagres og hentes riktig pa enhet`() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(AZURE).idToken

        val responsFraEnhet1Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/enheter/$lagretEnhet1/veiledere")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString
        val responsFraEnhet2Restkall = mockMvc.perform(MockMvcRequestBuilders.get("/api/veilederbehandling/enheter/$lagretEnhet2/veiledere")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString

        val typeRef = object : TypeReference<ArrayList<VeilederBrukerKnytningNoArgs>>() {}
        val knytningerPaEnhet1: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraEnhet1Restkall, typeRef)
        val knytningerPaEnhet2: ArrayList<VeilederBrukerKnytningNoArgs> = ObjectMapper().readValue(responsFraEnhet2Restkall, typeRef)

        assertThat(knytningerPaEnhet1).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent1, lagretFnr1, lagretEnhet1)) }
        assertThat(knytningerPaEnhet1).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent2, lagretFnr2, lagretEnhet1)) }
        assertThat(knytningerPaEnhet2).anyMatch { it.equals(VeilederBrukerKnytningNoArgs(lagretVeilederIdent2, lagretFnr3, lagretEnhet2)) }
    }


    private fun fjernBrukere() {
        tilknytningListe.forEach {
            veilederBehandlingDAO.slettVeilederBrukerKnytning(VeilederBrukerKnytning(it.veilederIdent, it.fnr, it.enhet))
        }
    }

    private class VeilederBrukerKnytningNoArgs(var veilederIdent: String = "", var fnr: String = "", var enhet: String = "") {
        fun equals(other: VeilederBrukerKnytningNoArgs): Boolean {
            return veilederIdent == other.veilederIdent && fnr == other.fnr && enhet == other.enhet
        }
    }

}
