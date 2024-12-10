package no.nav.syfo.client.kodeverk

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.azuread.AzureAdToken
import no.nav.syfo.testhelper.*
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime

class KodeverkClientSpek : Spek({

    val externalMockEnvironment = ExternalMockEnvironment()
    val redisStore = mockk<RedisStore>(relaxed = true)
    val azureAdClient = mockk<AzureAdClient>()

    val kodeverkClient = KodeverkClient(
        azureAdClient = azureAdClient,
        redisStore = redisStore,
        baseUrl = externalMockEnvironment.environment.kodeverkUrl,
        clientId = externalMockEnvironment.environment.kodeverkClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    coEvery {
        azureAdClient.getSystemToken(any())
    } returns AzureAdToken(
        accessToken = "token",
        expires = LocalDateTime.now().plusDays(1)
    )

    beforeEachTest {
        clearMocks(redisStore)
    }

    beforeGroup {
        externalMockEnvironment.startExternalMocks()
    }

    afterGroup {
        externalMockEnvironment.stopExternalMocks()
    }

    describe("${KodeverkClient::class.java.simpleName} getPostInformasjon") {
        val callId = "callId"

        it("returns cached value") {
            every { redisStore.getListObject<Postinformasjon>(any()) } returns postinformasjonList

            runBlocking {
                kodeverkClient.getPostinformasjon(callId) shouldBeEqualTo postinformasjonList
            }

            verify(exactly = 1) { redisStore.get(key = "postinformasjon") }
            verify(exactly = 0) { redisStore.setObject<Postinformasjon>(any(), any(), any()) }
        }

        it("fetch postinformasjon from kodeverk and caches value when cache is empty") {
            every { redisStore.getListObject<Postinformasjon>(any()) } returns null

            runBlocking {
                kodeverkClient.getPostinformasjon(callId) shouldBeEqualTo postinformasjonFromServerMock
            }

            verify(exactly = 1) { redisStore.get(key = "postinformasjon") }
            verify(exactly = 1) {
                redisStore.setObject(
                    expireSeconds = 86400,
                    key = "postinformasjon",
                    value = postinformasjonFromServerMock,
                )
            }
        }

        it("Don't store empty list in cache if call to Kodeverk fails") {
            every { redisStore.getListObject<Postinformasjon>(any()) } returns null

            runBlocking {
                kodeverkClient.getPostinformasjon(callId = "500")
            }

            verify(exactly = 1) { redisStore.get(key = "postinformasjon") }
            verify(exactly = 0) { redisStore.setObject<Postinformasjon>(any(), any(), any()) }
        }
    }
})

val postinformasjonList = listOf<Postinformasjon>(
    Postinformasjon(
        postnummer = "9990",
        poststed = "BÅTSFJORD",
    ),
)

val postinformasjonFromServerMock = listOf(
    Postinformasjon(
        postnummer = "1001",
        poststed = "OSLO",
    )
)
