@file:JvmName("StartKtor")

package no.nav.henvendelsesarkiv

fun main(args: Array<String>) {
    System.setProperty("APP_NAME", "Henvendelsearkiv")
    System.setProperty("APP_VERSION", "1.0")
    System.setProperty("HENVENDELSESARKIVDATASOURCE_URL", "jdbcUrl")
    System.setProperty("HENVENDELSESARKIVDATASOURCE_USERNAME", "jdbcUser")
    System.setProperty("HENVENDELSESARKIVDATASOURCE_PASSWORD", "jdbcPass")
    System.setProperty("ABAC_PDP_ENDPOINT_URL", "abac")
    System.setProperty("SRVHENVENDELSESARKIV2_USERNAME", "abacuser")
    System.setProperty("SRVHENVENDELSESARKIV2_PASSWORD", "abacpass")
    System.setProperty("SECURITY-TOKEN-SERVICE-JWKS_URL", "")
    System.setProperty("SECURITY-TOKEN-SERVICE-ISSUER_URL", "")
    System.setProperty("JWT_AUDIENCE", "AUD")
    System.setProperty("JWT_REALM", "REALM")

    createHttpServer(7070, "TESTING")
}