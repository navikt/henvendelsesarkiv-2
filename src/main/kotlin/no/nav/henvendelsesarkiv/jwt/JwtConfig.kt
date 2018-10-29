package no.nav.henvendelsesarkiv.jwt

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.auth.Principal
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal
import no.nav.henvendelsesarkiv.fasitProperties
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.URL
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("henvendelsesarkiv.JwtConfig")

class JwtConfig {

    val jwkProvider: JwkProvider = JwkProviderBuilder(URL(fasitProperties.jwksUrl))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

    fun validate(credentials: JWTCredential): Principal? {
        log.info("---------------------------")
        log.info(credentials.payload.issuer)
        log.info(credentials.payload.subject)
        log.info(credentials.payload.id)
        credentials.payload.audience.forEach { aud ->
            log.info("Audience: $aud")
        }
        credentials.payload.claims.forEach { (k, v) ->
            log.info( "Claim: ($k, ${v.asString()})" )
        }
        log.info("---------------------------")
        return try {
            requireNotNull(credentials.payload.audience) {"Audience not present"}
            // require(credentials.payload.audience.contains(fasitProperties.jwtAudience)) {"Wrong audience in claims"}
            JWTPrincipal(credentials.payload)
        } catch (e: Exception) {
            log.error("Failed to validate token", e)
            null
        }
    }
}