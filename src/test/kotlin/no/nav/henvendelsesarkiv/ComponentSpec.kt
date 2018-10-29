package no.nav.henvendelsesarkiv

import io.ktor.server.engine.ApplicationEngine
import org.amshove.kluent.*
import org.jetbrains.spek.api.*
import org.jetbrains.spek.api.dsl.*
import java.util.concurrent.TimeUnit

const val APP_NAME: String = "Application"
const val APP_VERSION: String = "1.0"

object ComponentSpec: Spek({
    lateinit var app: ApplicationEngine
    val url = "http://localhost:7070/"

    describe("Integration tests") {

        beforeGroup {
            System.setProperty("APP_NAME", APP_NAME)
            System.setProperty("APP_VERSION", APP_VERSION)
            System.setProperty("HENVENDELSESARKIVDATASOURCE_URL", "jdbcUrl")
            System.setProperty("HENVENDELSESARKIVDATASOURCE_USERNAME", "jdbcUser")
            System.setProperty("HENVENDELSESARKIVDATASOURCE_PASSWORD", "jdbcPass")
            System.setProperty("ABAC_PDP_ENDPOINT_URL", "abac")
            System.setProperty("SRVHENVENDELSESARKIV2_USERNAME", "abacuser")
            System.setProperty("SRVHENVENDELSESARKIV2_PASSWORD", "abacpass")
            System.setProperty("SECURITY-TOKEN-SERVICE-JWKS_URL", "https://jwt-provider-domain/")
            System.setProperty("SECURITY-TOKEN-SERVICE-ISSUER_URL", "https://jwt-provider-domain/")
            System.setProperty("JWT_AUDIENCE", "Audience")

            app = createHttpServer(7070, "TESTING")
        }

        given("application successfully started") {
            on("NAIS healthcheck") {
                it("isAlive answers with 200") {
                    val response = khttp.get(url + "isAlive")
                    response.statusCode `should equal` 200
                }

                it("isReady answers with 200") {
                    val response = khttp.get(url + "isReady")
                    response.statusCode `should equal` 200
                }
            }
        }

        afterGroup {
            app.stop(100, 100, TimeUnit.MILLISECONDS)
        }

    }
})