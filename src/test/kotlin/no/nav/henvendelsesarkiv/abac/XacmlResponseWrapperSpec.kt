package no.nav.henvendelsesarkiv.abac

import no.nav.henvendelsesarkiv.getFileAsString
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object XacmlResponseWrapperSpec : Spek({
    describe("XacmlResponseWrapper") {

        given("Response OK") {
            on("Simple response") {
                val wrapper = XacmlResponseWrapper(getResponse("xacml-simple-response.json"))
                it("should have descision deny") {
                    wrapper.getDecision() `should equal` Decision.Deny
                }
            }
        }

    }
})

private fun getResponse(fileName: String): String {
    return getFileAsString("src/test/resources/${fileName}")
}