package no.nav.henvendelsesarkiv

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.experimental.runBlocking

val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

class Application

fun main(args: Array<String>) {
    runBlocking {
        createHttpServer(applicationVersion = "henvendelsesarkiv-snapshot")
    }
}