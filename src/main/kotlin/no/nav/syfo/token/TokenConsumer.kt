package no.nav.syfo.token

import no.nav.syfo.util.EnvironmentUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.Objects.requireNonNull

@Component
class TokenConsumer(private val basicAuthRestTemplate: RestTemplate) {

    private val stsTokenUrl = EnvironmentUtil.getEnvVar("sts_token_url","https://sts.nav.no/sts/token")

    val token: Token
        get() {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

            val uriString = UriComponentsBuilder.fromHttpUrl(stsTokenUrl)
                    .queryParam("grant_type", "client_credentials")
                    .queryParam("scope", "openid")
                    .toUriString()

            val result = basicAuthRestTemplate.exchange(uriString, GET, HttpEntity<Any>(headers), Token::class.java)

            if (result.statusCode != OK) {
                throw RuntimeException("Henting av token feiler med HTTP-" + result.statusCode)
            }

            return requireNonNull<Token>(result.body)
        }
}

