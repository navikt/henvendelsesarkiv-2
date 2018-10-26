package no.nav.henvendelsesarkiv.routes

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.response.respondTextWriter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat

data class SelftestStatus(val status: String, val applicationVersion: String)

fun Routing.naisRoutes(applicationVersion: String,
                       collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry) {

    get("/isAlive") {
        call.respond(SelftestStatus(status = "I'm alive", applicationVersion = applicationVersion))
    }

    get("/isReady") {
        call.respond(SelftestStatus(status = "I'm ready", applicationVersion = applicationVersion))
    }

    get("/prometheus") {
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }

}