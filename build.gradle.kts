import org.jetbrains.kotlin.gradle.dsl.Coroutines

val junitJupiterVersion = "5.2.0"
val spekVersion = "1.1.5"
val hsqlDbVersion = "2.4.1"
val kluentVersion = "1.38"
val khttpVersion = "0.1.0"
val ktorVersion = "0.9.2"
val kotlinxVersion = "0.23.3"
val slf4jVersion = "1.8.0-beta2"
val jacksonVersion = "2.9.6"
val springJdbcVersion = "5.0.8.RELEASE"
val hikariCpVersion = "2.7.8"
val prometheusVersion = "0.4.0"

val mainClass = "no.nav.henvendelsesarkiv.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.2.60"
}

buildscript {
    repositories {
        maven("https://repo.adeo.no/repository/maven-central")
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

application {
    mainClassName = mainClass
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

dependencies {
    compile(kotlin("stdlib"))
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-gson:$ktorVersion")
    compile("org.slf4j:slf4j-simple:$slf4jVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    compile("org.springframework:spring-jdbc:$springJdbcVersion")
    compile("com.zaxxer:HikariCP:$hikariCpVersion")
    compile("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    compile("io.prometheus:simpleclient_common:$prometheusVersion")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")
    compile("khttp:khttp:$khttpVersion")

    testCompile("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testCompile("org.amshove.kluent:kluent:$kluentVersion")
    testCompile("org.hsqldb:hsqldb:$hsqlDbVersion")
    testCompile("org.jetbrains.spek:spek-api:$spekVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntime("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion") {
        exclude(group = "org.junit.platform")
        exclude(group = "org.jetbrains.kotlin")
    }
}

repositories {
    maven("https://repo.adeo.no/repository/maven-central")
    maven("https://plugins.gradle.org/m2/")
    maven("https://dl.bintray.com/kotlin/ktor/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "4.9"
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "henvendelsesarkiv-all"
    manifest {
        attributes["Implementation-Title"] = "Henvendelsesarkiv"
        attributes["Main-Class"] = mainClass
    }
    from(configurations.runtime.map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks["jar"] as CopySpec)
}
