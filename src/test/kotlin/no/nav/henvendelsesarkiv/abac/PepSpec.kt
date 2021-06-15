package no.nav.henvendelsesarkiv.abac

import no.nav.henvendelsesarkiv.jwt.SubjectPrincipal
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object PepSpec : Spek({
    describe("Pep") {
        given("henvendelse is calling") {
            on("with request to read") {
                it("should permit request") {
                    Pep.checkAccess(SubjectPrincipal(HENVENDELSE), Pep.Action.READ) `should equal` true
                }
            }
            on("with request to update") {
                it("should permit request") {
                    Pep.checkAccess(SubjectPrincipal(HENVENDELSE), Pep.Action.UPDATE) `should equal` true
                }
            }
            on("with any other type of request") {
                it("should deny request") {
                    Pep.checkAccess(SubjectPrincipal(HENVENDELSE), Pep.Action.CREATE) `should equal` false
                    Pep.checkAccess(SubjectPrincipal(HENVENDELSE), Pep.Action.DELETE) `should equal` false
                }
            }
        }

        given("dialogstyring is calling") {
            on("with request to create") {
                it("should permit request") {
                    Pep.checkAccess(SubjectPrincipal(DIALOGSTYRING), Pep.Action.CREATE) `should equal` true
                }
            }

            on("with any other type of request") {
                it("should deny request") {
                    Pep.checkAccess(SubjectPrincipal(DIALOGSTYRING), Pep.Action.READ) `should equal` false
                    Pep.checkAccess(SubjectPrincipal(DIALOGSTYRING), Pep.Action.UPDATE) `should equal` false
                    Pep.checkAccess(SubjectPrincipal(DIALOGSTYRING), Pep.Action.DELETE) `should equal` false
                }
            }
        }

        given("anyone else is calling") {
            on("any type of request") {
                it("should deny request") {
                    Pep.checkAccess(SubjectPrincipal("Any other subject"), Pep.Action.CREATE) `should equal` false
                    Pep.checkAccess(SubjectPrincipal("Any other subject"), Pep.Action.READ) `should equal` false
                    Pep.checkAccess(SubjectPrincipal("Any other subject"), Pep.Action.UPDATE) `should equal` false
                    Pep.checkAccess(SubjectPrincipal("Any other subject"), Pep.Action.DELETE) `should equal` false
                }
            }
        }
    }
})
