package no.nav.henvendelsesarkiv

class ApplicationProperties {
    val appName: String = getProperty(PropertyNames.APP_NAME)
    val appVersion: String = getProperty(PropertyNames.APP_VERSION)
    val dbUrl: String = getProperty(PropertyNames.HENVENDELSESARKIVDATASOURCE_URL)
    val dbUsername: String = getProperty(PropertyNames.HENVENDELSESARKIVDATASOURCE_USERNAME)
    val dbPassword: String = getProperty(PropertyNames.HENVENDELSESARKIVDATASOURCE_PASSWORD)
    val abacEndpoint: String = getProperty(PropertyNames.ABAC_PDP_ENDPOINT_URL)
    val abacUser: String = getProperty(PropertyNames.SRVHENVENDELSESARKIV2_USERNAME)
    val abacPass: String = getProperty(PropertyNames.SRVHENVENDELSESARKIV2_PASSWORD)
    val jwksUrl: String = getProperty(PropertyNames.SECURITY_TOKEN_SERVICE_JWKS_URL)
    val jwtIssuer: String = getProperty(PropertyNames.SECURITY_TOKEN_SERVICE_ISSUER_URL)
}

enum class PropertyNames {
    APP_NAME,
    APP_VERSION,
    HENVENDELSESARKIVDATASOURCE_URL,
    HENVENDELSESARKIVDATASOURCE_USERNAME,
    HENVENDELSESARKIVDATASOURCE_PASSWORD,
    ABAC_PDP_ENDPOINT_URL,
    SRVHENVENDELSESARKIV2_USERNAME,
    SRVHENVENDELSESARKIV2_PASSWORD,
    SECURITY_TOKEN_SERVICE_JWKS_URL,
    SECURITY_TOKEN_SERVICE_ISSUER_URL
}

fun setProperty(property: PropertyNames, value: String) {
    System.setProperty(property.name, value)
}

fun getProperty(property: PropertyNames, default: String? = null): String =
        System.getenv(property.name) ?: System.getProperty(property.name) ?: default ?: throw RuntimeException("Missing variable $property")