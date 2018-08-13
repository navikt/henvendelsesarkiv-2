package no.nav.henvendelsesarkiv

import io.javalin.Javalin
import org.amshove.kluent.*
import org.jetbrains.spek.api.*
import org.jetbrains.spek.api.dsl.*

const val APP_NAME: String = "Application"

object ComponentSpec: Spek({
    lateinit var app: Javalin
    val url = "http://localhost:7070/"

    describe("Integration tests") {

        beforeGroup {
            System.setProperty("APP_NAME", APP_NAME)
            System.setProperty("APP_VERSION", "Version")
            System.setProperty("HENVENDELSESARKIVDATASOURCE_URL", "jdbcUrl")
            System.setProperty("HENVENDELSESARKIVDATASOURCE_USERNAME", "jdbcUser")
            System.setProperty("HENVENDELSESARKIVDATASOURCE_PASSWORD", "jdbcPass")
            app = Application().init()
        }

        given("application successfully started") {
            on("NAIS healthcheck") {
                it("isAlive answers with 200") {
                    val response = khttp.get(url + "isAlive")
                    response.statusCode `should equal` 200
                }
            }

            on("NAIS healthcheck") {
                it("isReady answers with 200") {
                    val response = khttp.get(url + "isReady")
                    response.statusCode `should equal` 200
                }
            }

            on("checking fasit properties") {
                it("fasitTest answers with app name") {
                    val response = khttp.get(url + "fasitTest")
                    response.text `should equal` APP_NAME
                }
            }
        }

        afterGroup {
            app.stop()
        }

    }
})