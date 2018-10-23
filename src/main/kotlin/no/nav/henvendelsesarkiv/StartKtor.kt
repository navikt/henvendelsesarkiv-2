@file:JvmName("StartKtor")

package no.nav.henvendelsesarkiv

fun main(args: Array<String>) {
    System.setProperty("APP_NAME", "Henvendelsearkiv")
    System.setProperty("APP_VERSION", "1.0")
    System.setProperty("HENVENDELSESARKIVDATASOURCE_URL", "jdbcUrl")
    System.setProperty("HENVENDELSESARKIVDATASOURCE_USERNAME", "jdbcUser")
    System.setProperty("HENVENDELSESARKIVDATASOURCE_PASSWORD", "jdbcPass")
    System.setProperty("ABACPDP_URL", "abac")
    System.setProperty("ABAC_USERNAME", "abacuser")
    System.setProperty("ABAC_PASSWORD", "abacpass")

    createHttpServer(7070, "TESTING")
}