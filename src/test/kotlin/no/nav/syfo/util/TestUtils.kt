package no.nav.syfo.util

import com.nimbusds.jwt.SignedJWT
import no.nav.security.oidc.context.OIDCClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
import no.nav.security.oidc.context.TokenContext
import no.nav.security.spring.oidc.test.JwtTokenGenerator
import no.nav.syfo.util.OICDIssuer.INTERN


object TestUtils {
    //OIDC-hack - legg til token og oidcclaims for en testbruker
    fun loggInnSomVeileder(oidcRequestContextHolder: OIDCRequestContextHolder, veilederIdent: String) {
        val jwt : SignedJWT = JwtTokenGenerator.createSignedJWT(veilederIdent)
        val issuer = INTERN
        val tokenContext = TokenContext(issuer, jwt.serialize())
        val oidcClaims = OIDCClaims(jwt)
        val oidcValidationContext = OIDCValidationContext()
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims)
        oidcRequestContextHolder.setOIDCValidationContext(oidcValidationContext)
    }

    fun loggUt(oidcRequestContextHolder: OIDCRequestContextHolder) {
        oidcRequestContextHolder.setOIDCValidationContext(null)
    }

}
