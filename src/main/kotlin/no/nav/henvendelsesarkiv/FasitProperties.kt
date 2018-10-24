package no.nav.henvendelsesarkiv

data class FasitProperties(
        val appName: String = getEnvVar("APP_NAME"),
        val appVersion: String = getEnvVar("APP_VERSION"),
        val dbUrl: String = getEnvVar("HENVENDELSESARKIVDATASOURCE_URL"),
        val dbUsername: String = getEnvVar("HENVENDELSESARKIVDATASOURCE_USERNAME"),
        val dbPassword: String = getEnvVar("HENVENDELSESARKIVDATASOURCE_PASSWORD"),
        val abacEndpoint: String = getEnvVar("ABAC_PDP_ENDPOINT_URL"),
        val abacUser: String = getEnvVar("SRVHENVENDELSESARKIV2_USERNAME"),
        val abacPass: String = getEnvVar("SRVHENVENDELSESARKIV2_PASSWORD")
)

fun getEnvVar(name: String, default: String? = null): String =
        System.getenv(name) ?: System.getProperty(name) ?: default ?: throw RuntimeException("Missing variable $name")