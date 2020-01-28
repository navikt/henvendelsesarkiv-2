package no.nav.henvendelsesarkiv

import no.nav.common.nais.utils.NaisUtils
import no.nav.henvendelsesarkiv.PropertyNames.*
import no.nav.henvendelsesarkiv.db.UpdateService
import no.nav.henvendelsesarkiv.db.hikariDatasource
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

private const val FEM_MINUTTER: Long = 1000 * 60 * 5
private val log = LoggerFactory.getLogger("henvendelsesarkiv.Application")

data class ApplicationState(
        val properties: ApplicationProperties,
        var running: Boolean = true,
        var initialized: Boolean = false
)

fun main() {
    runDatabaseMigrationOnStartup()
    loadEnvironmentFromVault()

    val applicationState = ApplicationState(ApplicationProperties())
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

private fun runDatabaseMigrationOnStartup() {
    val flyway = Flyway()
    flyway.dataSource = hikariDatasource
    flyway.migrate()
}

private fun loadEnvironmentFromVault() {
    val serviceUser = NaisUtils.getCredentials("service_user")
    setProperty(SRVHENVENDELSESARKIV2_USERNAME, serviceUser.username)
    setProperty(SRVHENVENDELSESARKIV2_PASSWORD, serviceUser.password)

    val dbUser = NaisUtils.getCredentials("db_user")
    setProperty(HENVENDELSESARKIVDATASOURCE_USERNAME, dbUser.username)
    setProperty(HENVENDELSESARKIVDATASOURCE_PASSWORD, dbUser.password)
}

private fun startKasseringsjobb(timer: Timer) {
    log.info("Starter kasseringsjobb.")
    timer.schedule(FEM_MINUTTER, FEM_MINUTTER) {
        try {
            UpdateService().kasserUtgaatteHenvendelser()
        } catch (e: Exception) {
            log.error("Kassering feilet, men schedulering må overleve, så dette bare logges", e)
        }
    }

}