package no.nav.henvendelsesarkiv

import kotlinx.coroutines.experimental.runBlocking

class Application

fun main(args: Array<String>) {
    runBlocking {
        createHttpServer(applicationVersion = "henvendelsesarkiv-snapshot")
    }
}