package no.nav.syfo.util

import no.nav.security.oidc.context.OIDCClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
import no.nav.security.oidc.context.TokenContext
import no.nav.security.spring.oidc.test.JwtTokenGenerator
import no.nav.syfo.util.OIDCIssuer.AZURE


object TestUtils {
    //OIDC-hack - legg til token og oidcclaims for en testbruker
    fun loggInnSomVeileder(oidcRequestContextHolder: OIDCRequestContextHolder, veilederIdent: String) {
        val jwt = JwtTokenGenerator.createSignedJWT(veilederIdent)
        val issuer = AZURE
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
