package no.nav.henvendelsesarkiv

import no.nav.henvendelsesarkiv.db.DatabaseService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

private const val FEM_MINUTTER: Long = 1000 * 60 * 5
private val log = LoggerFactory.getLogger("henvendelsesarkiv.Application")

val fasitProperties = FasitProperties()

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

fun main() {
    val applicationState = ApplicationState()
    val applicationServer = createHttpServer(applicationState = applicationState)
    val kasseringstimer = Timer()

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutdown hook called, shutting down gracefully")
        kasseringstimer.cancel()
        applicationState.initialized = false
        applicationServer.stop(5, 5, TimeUnit.SECONDS)
    })

    startKasseringsjobb(kasseringstimer)

    applicationServer.start(wait = true)
}

private fun startKasseringsjobb(timer: Timer) {
    log.info("Starter kasseringsjobb.")
    timer.schedule(FEM_MINUTTER, FEM_MINUTTER) {
        try {
            DatabaseService().kasserUtgaatteHenvendelser()
        } catch (e: Exception) {
            log.error("Kassering feilet, men schedulering må overleve, så dette bare logges", e)
        }
    }

}