package no.nav.henvendelsesarkiv

import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.routing.accept
import io.ktor.routing.routing
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
import org.slf4j.event.Level
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

    install(CallLogging) {
        level = Level.INFO
    }

    routing {
        naisRoutes(applicationVersion)

        authenticate {
            accept(ContentType.Application.Json) {
                arkivpostRoutes(PepClient(Decision.Deny))
            }
        }

    }
}.start(wait = wait)
