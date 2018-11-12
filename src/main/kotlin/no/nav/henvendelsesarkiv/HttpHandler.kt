package no.nav.henvendelsesarkiv

import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.routing.*
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.henvendelsesarkiv.abac.Decision
import no.nav.henvendelsesarkiv.abac.PepClient
import no.nav.henvendelsesarkiv.db.localDateTimeDeserializer
import no.nav.henvendelsesarkiv.db.localDateTimeSerializer
import no.nav.henvendelsesarkiv.jwt.JwtConfig
import no.nav.henvendelsesarkiv.routes.arkivpostRoutes
import no.nav.henvendelsesarkiv.routes.naisRoutes
import java.time.LocalDateTime

private const val REALM = "Henvendelsesarkiv JWT Realm"

fun createHttpServer(port: Int = 7070, applicationVersion: String, wait: Boolean = true): ApplicationEngine = embeddedServer(Netty, port) {
    install(Authentication) {
        jwt {
            val jwtConfig = JwtConfig()
            realm = REALM
            verifier(jwtConfig.jwkProvider, fasitProperties.jwtIssuer)
            validate { credentials ->
                jwtConfig.validate(credentials)
            }
        }
    }

    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(LocalDateTime::class.java, localDateTimeSerializer)
            registerTypeAdapter(LocalDateTime::class.java, localDateTimeDeserializer)
            setPrettyPrinting()
            disableHtmlEscaping()
        }
    }

    routing {
        naisRoutes(applicationVersion)

        //authenticate {
        arkivpostRoutes(PepClient(Decision.Deny))
        //}

    }
}.start(wait = wait)
