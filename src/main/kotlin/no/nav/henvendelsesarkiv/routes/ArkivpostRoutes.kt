package no.nav.henvendelsesarkiv.routes

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.pipeline.PipelineContext
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import no.nav.henvendelsesarkiv.*
import no.nav.henvendelsesarkiv.abac.PepClient

fun Route.arkivpostRoutes(pepClient: PepClient) {

    get("/arkivpost/{arkivpostId}") {
        if (!pepClient.checkAccess(call.request.header("Authorization"), "read"))
            call.respond(io.ktor.http.HttpStatusCode.Forbidden)
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
    val arkivpostId = call.parameters["arkivpostId"]?.toLong()
    if (arkivpostId == null) {
        call.respond(io.ktor.http.HttpStatusCode.BadRequest)
    } else {
        val arkivpost = no.nav.henvendelsesarkiv.DatabaseService(no.nav.henvendelsesarkiv.hikariJdbcTemplate).hentHenvendelse(arkivpostId)
        if (arkivpost == null) {
            call.respond(io.ktor.http.HttpStatusCode.NotFound)
        } else {
            call.respond(arkivpost)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.hentTemagrupper() {
    val aktoerId = call.parameters["aktørId"]
    if (aktoerId == null) {
        call.respond(io.ktor.http.HttpStatusCode.BadRequest)
    } else {
        call.respond(no.nav.henvendelsesarkiv.DatabaseService(no.nav.henvendelsesarkiv.hikariJdbcTemplate).hentTemagrupper(aktoerId))
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.settUtgaarDato() {
    val arkivpostId = call.parameters["arkivpostId"]?.toLong()
    val post = call.receive<Parameters>()
    val utgaarDato = post["utgaarDato"]?.let(::lagDateTime)
    if (arkivpostId == null || utgaarDato == null) {
        call.respond(io.ktor.http.HttpStatusCode.BadRequest)
    } else {
        no.nav.henvendelsesarkiv.DatabaseService(no.nav.henvendelsesarkiv.hikariJdbcTemplate).settUtgaarDato(arkivpostId, utgaarDato)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.hentArkivpostForAktoer() {
    val aktoerId = call.parameters["aktørId"]
    val fra = call.request.queryParameters["fra"]?.let(::lagDateTime)
    val til = call.request.queryParameters["til"]?.let(::lagDateTime)
    val max = call.request.queryParameters["max"]?.toInt()

    if (aktoerId == null) {
        call.respond(io.ktor.http.HttpStatusCode.BadRequest)
    } else {
        call.respond(no.nav.henvendelsesarkiv.DatabaseService(no.nav.henvendelsesarkiv.hikariJdbcTemplate).hentHenvendelserForAktoer(aktoerId, fra, til, max))
    }
}

