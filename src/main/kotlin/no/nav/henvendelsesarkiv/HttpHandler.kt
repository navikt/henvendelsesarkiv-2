package no.nav.henvendelsesarkiv

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.pipeline.PipelineContext
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondWrite
import io.ktor.routing.*
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.henvendelsesarkiv.abac.Decision
import no.nav.henvendelsesarkiv.abac.PepClient
import no.nav.henvendelsesarkiv.jwt.JwtConfig
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private const val REALM = "Henvendelsesarkiv JWT Realm"
private val log = LoggerFactory.getLogger("henvendelsesarkiv.HttpServer")
private val prometheusContentType = ContentType.parse(TextFormat.CONTENT_TYPE_004)
private val pdpClient = PepClient(Decision.Deny)

val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry

data class SelftestStatus(val status: String, val applicationVersion: String)

fun createHttpServer(port: Int = 7070, applicationVersion: String): ApplicationEngine = embeddedServer(Netty, port) {
    install(Authentication) {
        jwt("oidc-auth") {
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
        }
    }

    routing {
        naisRoutes(applicationVersion)

        authenticate("oidc-auth") {
            arkivRoutes()
        }

    }
}.start()

private fun Route.arkivRoutes() {
    get("/arkivpost/{arkivpostId}") {
        hentArkivpost()
    }

    post("/arkivpost") {
        call.respond(DatabaseService(hikariJdbcTemplate).opprettHenvendelse(call.receive()))
    }

    get("/temagrupper/{aktørId}") {
        hentTemagrupper()
    }

    post("/arkivpost/{arkivpostId}/utgaar") {
        settUtgaarDato()
    }

    get("/arkivpost/aktoer/{aktørId}") {
        hentArkivpostForAktoer()
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.hentArkivpost() {
    if (!checkAccess(call.request.header("Authorization"), "read")) call.respond(HttpStatusCode.Forbidden)
    val arkivpostId = call.parameters["arkivpostId"]?.toLong()
    if (arkivpostId == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        val arkivpost = DatabaseService(hikariJdbcTemplate).hentHenvendelse(arkivpostId)
        if (arkivpost == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(arkivpost)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.hentTemagrupper() {
    val aktoerId = call.parameters["aktørId"]
    if (aktoerId == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        call.respond(DatabaseService(hikariJdbcTemplate).hentTemagrupper(aktoerId))
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.settUtgaarDato() {
    val arkivpostId = call.parameters["arkivpostId"]?.toLong()
    val post = call.receive<Parameters>()
    val utgaarDato = post["utgaarDato"]?.let(::lagDateTime)
    if (arkivpostId == null || utgaarDato == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        DatabaseService(hikariJdbcTemplate).settUtgaarDato(arkivpostId, utgaarDato)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.hentArkivpostForAktoer() {
    val aktoerId = call.parameters["aktørId"]
    val fra = call.request.queryParameters["fra"]?.let(::lagDateTime)
    val til = call.request.queryParameters["til"]?.let(::lagDateTime)
    val max = call.request.queryParameters["max"]?.toInt()

    if (aktoerId == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        call.respond(DatabaseService(hikariJdbcTemplate).hentHenvendelserForAktoer(aktoerId, fra, til, max))
    }
}

private fun Route.naisRoutes(applicationVersion: String) {
    get("/isAlive") {
        call.respond(SelftestStatus(status = "I'm alive", applicationVersion = applicationVersion))
    }

    get("/isReady") {
        call.respond(SelftestStatus(status = "I'm ready", applicationVersion = applicationVersion))
    }

    get("/prometheus") {
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
        call.respondWrite(prometheusContentType) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}

private fun checkAccess(bearerToken: String?, action: String): Boolean {
    requireNotNull(bearerToken) {"Authorization token not set"}
    val token = bearerToken!!.substringAfter(" ")
    return pdpClient.hasAccessToResource(extractBodyFromOidcToken(token),  action)
}

private fun extractBodyFromOidcToken(token: String): String {
    return token.substringAfter(".").substringBefore(".")
}
