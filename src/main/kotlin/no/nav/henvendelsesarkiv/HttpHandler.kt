package no.nav.henvendelsesarkiv

import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.henvendelsesarkiv.db.localDateTimeDeserializer
import no.nav.henvendelsesarkiv.db.localDateTimeSerializer
import no.nav.henvendelsesarkiv.jwt.JwtConfig
import no.nav.henvendelsesarkiv.routes.arkivpostRoutes
import no.nav.henvendelsesarkiv.routes.naisRoutes
import org.slf4j.event.Level
import java.time.LocalDateTime

private const val REALM = "Henvendelsesarkiv JWT Realm"

fun createHttpServer(applicationState: ApplicationState, port: Int = 8080): ApplicationEngine = embeddedServer(Netty, port) {
    install(StatusPages) {
        notFoundHandler()
        exceptionHandler()
    }

    install(Authentication) {
        jwt {
            val jwtConfig = JwtConfig(applicationState.properties)
            realm = REALM
            verifier(jwtConfig.jwkProvider, applicationState.properties.jwtIssuer)
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
        level = Level.TRACE
        filter { call -> call.request.path().startsWith("/arkivpost") }
        filter { call -> call.request.path().startsWith("/temagrupper") }
    }

    routing {
        naisRoutes(readinessCheck = { applicationState.initialized }, livenessCheck = { applicationState.running })

        authenticate {
            arkivpostRoutes()
        }
    }
    applicationState.initialized = true
}
