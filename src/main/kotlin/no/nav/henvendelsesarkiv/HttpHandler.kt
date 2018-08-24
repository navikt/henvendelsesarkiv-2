package no.nav.henvendelsesarkiv

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondWrite
import io.ktor.routing.Route
import io.ktor.routing.accept
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import org.slf4j.LoggerFactory

val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
private val log = LoggerFactory.getLogger("faktum.HttpServer")
private val prometheusContentType = ContentType.parse(TextFormat.CONTENT_TYPE_004)
private val fasitProperties = FasitProperties()

data class SelftestStatus(val status: String, val applicationVersion: String)

fun createHttpServer(port: Int = 7070, applicationVersion: String): ApplicationEngine = embeddedServer(Netty, port) {
    routing {
        accept(ContentType.Application.Json) {
            jsonRoutes(applicationVersion)
        }

        accept(ContentType.Any) {
            anyRoutes()
        }
    }
}.start()

suspend fun ApplicationCall.respondJson(json: suspend () -> Any) {
    respondWrite {
        objectMapper.writeValue(this, json.invoke())
    }
}

suspend fun ApplicationCall.respondJson(input: Any) {
    respondWrite(ContentType.Application.Json) {
        objectMapper.writeValue(this, input)
    }
}

private fun Route.jsonRoutes(applicationVersion: String) {
    get("/hentarkivpost/{arkivpostId}") {
        val arkivpostId = call.parameters["arkivpostId"]?.toLong()
        if (arkivpostId == null) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            val arkivpost = DatabaseService(hikariJdbcTemplate).hentHenvendelse(arkivpostId)
            if (arkivpost == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respondJson(arkivpost)
            }
        }
    }

    get("/isAlive") {
        call.respondJson(SelftestStatus(status = "I'm alive", applicationVersion = applicationVersion))
    }

    get("/isReady") {
        call.respondJson(SelftestStatus(status = "I'm ready", applicationVersion = applicationVersion))
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