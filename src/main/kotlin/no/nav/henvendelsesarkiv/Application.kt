package no.nav.henvendelsesarkiv

import kotlinx.coroutines.runBlocking

val fasitProperties = FasitProperties()

class Application

fun main(args: Array<String>) {
    runBlocking {
        createHttpServer(applicationVersion = "henvendelsesarkiv-snapshot")
    }
}