package no.nav.henvendelsesarkiv.routes

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext
import no.nav.henvendelsesarkiv.abac.PepClient
import no.nav.henvendelsesarkiv.db.DatabaseService
import no.nav.henvendelsesarkiv.db.lagDateTime
import no.nav.henvendelsesarkiv.model.Arkivpost
import org.slf4j.LoggerFactory

fun Route.arkivpostRoutes(pepClient: PepClient) {
    val log = LoggerFactory.getLogger("henvendelsesarkiv.ArkivpostRoutes")


    get("/arkivpost/{arkivpostId}") {
        /*if (!pepClient.checkAccess(call.request.header("Authorization"), "read"))
            call.respond(HttpStatusCode.Forbidden)*/
        hentArkivpost()
    }

    post("/arkivpost") {
        val arkivpost: Arkivpost = call.receive()
        val arkivpostId = DatabaseService().opprettHenvendelse(arkivpost)
        call.respond(arkivpostId)
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
        call.respond(HttpStatusCode.BadRequest)
    } else {
        val arkivpost = DatabaseService().hentHenvendelse(arkivpostId)
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
        call.respond(DatabaseService().hentTemagrupper(aktoerId))
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.settUtgaarDato() {
    val arkivpostId = call.parameters["arkivpostId"]?.toLong()
    val post = call.receive<Parameters>()
    val utgaarDato = post["utgaarDato"]?.let(::lagDateTime)
    if (arkivpostId == null || utgaarDato == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        DatabaseService().settUtgaarDato(arkivpostId, utgaarDato)
        call.respond(HttpStatusCode.OK)
    }
    //TODO: Burde det kastes 500-feil om den kommer hit? Gjelder kanskje flere tjenestekall?
}

private suspend fun PipelineContext<Unit, ApplicationCall>.hentArkivpostForAktoer() {
    val aktoerId = call.parameters["aktørId"]
    val fra = call.request.queryParameters["fra"]?.let(::lagDateTime)
    val til = call.request.queryParameters["til"]?.let(::lagDateTime)
    val max = call.request.queryParameters["max"]?.toInt()

    if (aktoerId == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        call.respond(DatabaseService().hentHenvendelserForAktoer(aktoerId, fra, til, max))
    }
}