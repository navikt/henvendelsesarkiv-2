package no.nav.henvendelsesarkiv.jwt

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.auth.Principal
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal
import no.nav.henvendelsesarkiv.ApplicationProperties
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.URL
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("henvendelsesarkiv.JwtConfig")

class JwtConfig(applicationProperties: ApplicationProperties) {

    val jwkProvider: JwkProvider = JwkProviderBuilder(URL(applicationProperties.jwksUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    fun validate(credentials: JWTCredential): Principal? {
        return try {
            requireNotNull(credentials.payload.audience) { "Audience not present" }
            JWTPrincipal(credentials.payload)
        } catch (e: Exception) {
            log.error("Failed to validate token", e)
            null
        }
    }
}
