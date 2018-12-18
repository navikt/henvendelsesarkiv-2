package no.nav.henvendelsesarkiv

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("henvendelsesarkiv.Application")

val fasitProperties = FasitProperties()

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

fun main(args: Array<String>) {
    val applicationState = ApplicationState()
    val applicationServer = createHttpServer(applicationState = applicationState)

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutdown hook called, shutting down gracefully")
        applicationState.initialized = false
        applicationServer.stop(5, 5, TimeUnit.SECONDS)
    })

    applicationServer.start(wait = true)
}