package no.nav.henvendelsesarkiv.abac

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import no.nav.henvendelsesarkiv.jwt.SubjectPrincipal

const val HENVENDELSE = "srvHenvendelse"
const val DIALOGSTYRING = "srvdialogstyring"
private const val PERMIT = true
private const val DENY = false

object Pep {
    enum class Action {
        CREATE, READ, UPDATE, DELETE
    }

    fun checkAccess(principal: SubjectPrincipal?, action: Action): Boolean {
        requireNotNull(principal) { "No principal found" }
        val subject = principal.subject
        return if ((action == Action.READ || action == Action.UPDATE) && subject == HENVENDELSE) {
            PERMIT
        } else if (action == Action.CREATE && subject == DIALOGSTYRING) {
            PERMIT
        } else {
            DENY
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.withAccess(
    action: Pep.Action,
    block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    val principal: SubjectPrincipal? = this.call.authentication.principal()
    if (!Pep.checkAccess(principal, action)) {
        call.respond(HttpStatusCode.Forbidden)
    } else {
        block(this)
    }
}