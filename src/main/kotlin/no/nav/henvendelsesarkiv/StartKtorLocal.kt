package no.nav.henvendelsesarkiv

import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("henvendelsesarkiv.Application")

fun main(args: Array<String>) {
    val properties = Properties()
    properties.load(FileInputStream(System.getProperty("user.home") + "/localstart.properties"))

    System.setProperty("APP_NAME", "Henvendelsearkiv")
    System.setProperty("APP_VERSION", "1.0")
    System.setProperty("HENVENDELSESARKIVDATASOURCE_URL", "jdbcUrl")
    System.setProperty("HENVENDELSESARKIVDATASOURCE_USERNAME", "jdbcUser")
    System.setProperty("HENVENDELSESARKIVDATASOURCE_PASSWORD", "jdbcPass")
    System.setProperty("ABAC_PDP_ENDPOINT_URL", properties.getProperty("ABAC_PDP_ENDPOINT_URL"))
    System.setProperty("SRVHENVENDELSESARKIV2_USERNAME", properties.getProperty("SRVHENVENDELSESARKIV2_USERNAME"))
    System.setProperty("SRVHENVENDELSESARKIV2_PASSWORD", properties.getProperty("SRVHENVENDELSESARKIV2_PASSWORD"))
    System.setProperty("SECURITY_TOKEN_SERVICE_JWKS_URL", properties.getProperty("SECURITY-TOKEN-SERVICE-JWKS_URL"))
    System.setProperty("SECURITY_TOKEN_SERVICE_ISSUER_URL", properties.getProperty("SECURITY-TOKEN-SERVICE-ISSUER_URL"))

    val applicationState = ApplicationState()
    val applicationServer = createHttpServer(applicationState, 7070)

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutdown hook called, shutting down gracefully")
        applicationState.initialized = false
        applicationServer.stop(1, 1, TimeUnit.SECONDS)
    })

    applicationServer.start(wait = true)
}