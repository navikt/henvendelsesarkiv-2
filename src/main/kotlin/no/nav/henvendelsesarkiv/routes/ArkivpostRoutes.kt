package no.nav.henvendelsesarkiv.routes

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext
import no.nav.henvendelsesarkiv.abac.PepClient
import no.nav.henvendelsesarkiv.db.SelectService
import no.nav.henvendelsesarkiv.db.UpdateService
import no.nav.henvendelsesarkiv.db.lagDateTime
import no.nav.henvendelsesarkiv.model.Arkivpost
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("henvendelsesarkiv.ArkivpostRoutes")

fun Route.arkivpostRoutes(pepClient: PepClient) {
    get("/arkivpost/{arkivpostId}") {
        if (!pepClient.checkAccess(call.request.header("Authorization"), "hentArkivpost", "read"))
            call.respond(HttpStatusCode.Forbidden)
        hentArkivpost()
    }

    get("/temagrupper/{aktørId}") {
        if (!pepClient.checkAccess(call.request.header("Authorization"), "hentTemagrupper", "read"))
            call.respond(HttpStatusCode.Forbidden)
        hentTemagrupper()
    }

    get("/arkivpost/aktoer/{aktørId}") {
        if (!pepClient.checkAccess(call.request.header("Authorization"), "hentArkivpostForAktør", "read"))
            call.respond(HttpStatusCode.Forbidden)
        hentArkivpostForAktoer()
    }

    post("/arkivpost") {
        if (!pepClient.checkAccess(call.request.header("Authorization"), "opprettArkivpost", "create"))
            call.respond(HttpStatusCode.Forbidden)
        val arkivpost: Arkivpost = call.receive()
        val arkivpostId = UpdateService().opprettHenvendelse(arkivpost)
        call.respond(arkivpostId)
    }

    post("/arkivpost/{arkivpostId}/utgaar") {
        if (!pepClient.checkAccess(call.request.header("Authorization"), "settUtgårDato", "update"))
            call.respond(HttpStatusCode.Forbidden)
        try {
            settUtgaarDato()
        } catch (e: Throwable) {
            log.error("Feil i utgår dato", e)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.hentArkivpost() {
    val arkivpostId = call.parameters["arkivpostId"]?.toLong()
    if (arkivpostId == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        val arkivpost = SelectService().hentHenvendelse(arkivpostId)
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
        call.respond(SelectService().hentTemagrupper(aktoerId))
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.settUtgaarDato() {
    val arkivpostId = call.parameters["arkivpostId"]?.toLong()
    val post = call.receive<Parameters>()
    val utgaarDato = post["utgaarDato"]?.let(::lagDateTime)
    if (arkivpostId == null || utgaarDato == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        UpdateService().settUtgaarDato(arkivpostId, utgaarDato)
        call.respond(HttpStatusCode.OK)
    }
    // TODO: Burde det kastes 500-feil om den kommer hit? Gjelder kanskje flere tjenestekall?
}

private suspend fun PipelineContext<Unit, ApplicationCall>.hentArkivpostForAktoer() {
    val aktoerId = call.parameters["aktørId"]
    val fra = call.request.queryParameters["fra"]?.let(::lagDateTime)
    val til = call.request.queryParameters["til"]?.let(::lagDateTime)
    val max = call.request.queryParameters["max"]?.toInt()

    if (aktoerId == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        call.respond(SelectService().hentHenvendelserForAktoer(aktoerId, fra, til, max))
    }
}
