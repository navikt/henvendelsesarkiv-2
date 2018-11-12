package no.nav.henvendelsesarkiv

val fasitProperties = FasitProperties()

fun main(args: Array<String>) {
    createHttpServer(applicationVersion = "henvendelsesarkiv-snapshot")
}