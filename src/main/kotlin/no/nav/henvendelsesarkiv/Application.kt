package no.nav.henvendelsesarkiv

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    Application().init()
}

class Application(private val port: Int = 7070) {

    fun init(): Javalin {
        val log = LoggerFactory.getLogger("server-init")
        val fasitProperties = FasitProperties()

        return Javalin.create().apply {
            port(port)
            enableCaseSensitiveUrls()
            exception(Exception::class.java) { ex, ctx ->
                log.warn("An error occuredd", ex)
                ctx.status(500)
            }
            error(404) { ctx -> ctx.json("${ctx.url()} not found") }
        }.routes {
            setUpRoutes(fasitProperties)
        }.after {
            it.header("Server", "Henvendelsesarkiv")
        }.start()
    }

    private fun setUpRoutes(fasitProperties: FasitProperties) {
        path("/fasitTest") {
            get { ctx -> ctx.result(fasitProperties.dbUsername) }
        }

        path("/isAlive") {
            get { it.status(200) }
        }

        path("/isReady") {
            get { it.status(200) }
        }
    }

}