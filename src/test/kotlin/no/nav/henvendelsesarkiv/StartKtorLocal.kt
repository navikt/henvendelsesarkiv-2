package no.nav.henvendelsesarkiv

import no.nav.common.nais.utils.NaisYamlUtils
import no.nav.common.nais.utils.NaisYamlUtils.getTemplatedConfig
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("henvendelsesarkiv.Application")

fun main() {
    NaisYamlUtils.loadFromYaml(getTemplatedConfig(".nais/qa-template.yaml", mapOf(
            "namespace" to "q0",
            "dbUrl" to "jdbc:oracle:thin:@a01dbfl033.adeo.no:1521/henvendelsearkiv_q0"
    )))
    val propsResource = NaisYamlUtils::class.java.classLoader.getResourceAsStream(".vault.properties")
    System.getProperties().load(propsResource)

    val applicationState = ApplicationState(ApplicationProperties())
    val applicationServer = createHttpServer(applicationState, 7072)

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutdown hook called, shutting down gracefully")
        applicationState.initialized = false
        applicationServer.stop(1, 1, TimeUnit.SECONDS)
    })

    applicationServer.start(wait = true)
}