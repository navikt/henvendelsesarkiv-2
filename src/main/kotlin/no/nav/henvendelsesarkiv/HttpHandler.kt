package no.nav.henvendelsesarkiv

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.pipeline.PipelineContext
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondWrite
import io.ktor.routing.*
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.henvendelsesarkiv.model.Arkivpost
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
private val log = LoggerFactory.getLogger("faktum.HttpServer")
private val prometheusContentType = ContentType.parse(TextFormat.CONTENT_TYPE_004)
private val fasitProperties = FasitProperties()

data class SelftestStatus(val status: String, val applicationVersion: String)

fun createHttpServer(port: Int = 7070, applicationVersion: String): ApplicationEngine = embeddedServer(Netty, port) {
    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(LocalDateTime::class.java, localDateTimeSerializer)
            registerTypeAdapter(LocalDateTime::class.java, localDateTimeDeserializer)
            setPrettyPrinting()
        }
    }

    routing {
        accept(ContentType.Application.Json) {
            jsonRoutes(applicationVersion)
        }

        accept(ContentType.Any) {
            anyRoutes()
        }
    }
}.start()

private fun Route.jsonRoutes(applicationVersion: String) {
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

    get("/isAlive") {
        call.respond(SelftestStatus(status = "I'm alive", applicationVersion = applicationVersion))
    }

    get("/isReady") {
        call.respond(SelftestStatus(status = "I'm ready", applicationVersion = applicationVersion))
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.hentArkivpost() {
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

private fun Route.anyRoutes() {
    get("/fasitTest") {
        call.respondText(fasitProperties.dbUsername, ContentType.Text.Plain)
    }

    get("/isAlive") {
        call.respondText("I'm alive.", ContentType.Text.Plain)
    }

    get("/isReady") {
        call.respondText("I'm ready.", ContentType.Text.Plain)
    }

    get("/prometheus") {
        log.info("Responding to prometheus request.")
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
        call.respondWrite(prometheusContentType) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}