package no.nav.henvendelsesarkiv

class ApplicationProperties {
    val appName: String = getProperty(FasitPropertyNames.APP_NAME)
    val appVersion: String = getProperty(FasitPropertyNames.APP_VERSION)
    val dbUrl: String = getProperty(FasitPropertyNames.HENVENDELSESARKIVDATASOURCE_URL)
    val dbUsername: String = getProperty(FasitPropertyNames.HENVENDELSESARKIVDATASOURCE_USERNAME)
    val dbPassword: String = getProperty(FasitPropertyNames.HENVENDELSESARKIVDATASOURCE_PASSWORD)
    val abacEndpoint: String = getProperty(FasitPropertyNames.ABAC_PDP_ENDPOINT_URL)
    val abacUser: String = getProperty(FasitPropertyNames.SRVHENVENDELSESARKIV2_USERNAME)
    val abacPass: String = getProperty(FasitPropertyNames.SRVHENVENDELSESARKIV2_PASSWORD)
    val jwksUrl: String = getProperty(FasitPropertyNames.SECURITY_TOKEN_SERVICE_JWKS_URL)
    val jwtIssuer: String = getProperty(FasitPropertyNames.SECURITY_TOKEN_SERVICE_ISSUER_URL)
}

enum class FasitPropertyNames {
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

fun getProperty(property: FasitPropertyNames, default: String? = null): String =
        System.getenv(property.name) ?: System.getProperty(property.name) ?: default ?: throw RuntimeException("Missing variable $property")