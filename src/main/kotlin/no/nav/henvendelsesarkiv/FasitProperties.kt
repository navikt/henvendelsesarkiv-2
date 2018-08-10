package no.nav.henvendelsesarkiv

data class FasitProperties(
        val appName: String = getEnvVar("APP_NAME"),
        val appVersion: String = getEnvVar("APP_VERSION"),
        val dbUrl: String = getEnvVar("HENVENDELSESARKIVDATASOURCE_URL"),
        val dbUsername: String = getEnvVar("HENVENDELSESARKIVDATASOURCE_USERNAME"),
        val dbPassword: String = getEnvVar("HENVENDELSESARKIVDATASOURCE_PASSWORD")
)

fun getEnvVar(name: String, default: String? = null): String =
        System.getenv(name) ?: default ?: throw RuntimeException("Missing variable $name")