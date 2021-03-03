

val junitJupiterVersion = "5.3.0"
val spekVersion = "1.2.1"
val hsqlDbVersion = "2.4.1"
val kluentVersion = "1.42"
val khttpVersion = "0.1.0"
val ktorVersion = "1.0.0"
val logbackVersion = "1.2.3"
val logstashVersion = "5.1"
val springJdbcVersion = "5.1.1.RELEASE"
val hikariCpVersion = "3.2.0"
val prometheusVersion = "0.4.0"
val ojdbcVersion = "19.3.0.0"
val flywayVersion = "4.2.0"
val naisUtilsVersion = "1.2020.01.23-17.30-c6684f7b3098"

val mainClass = "no.nav.henvendelsesarkiv.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.3.0"
    id("org.flywaydb.flyway") version "5.1.4"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

application {
    mainClassName = mainClass
}

dependencies {
    compile(kotlin("stdlib"))
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-gson:$ktorVersion")
    compile("io.ktor:ktor-auth:$ktorVersion")
    compile("io.ktor:ktor-auth-jwt:$ktorVersion")
    compile("io.ktor:ktor-client-auth-basic:$ktorVersion")
    compile("ch.qos.logback:logback-classic:$logbackVersion")
    compile("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    compile("org.springframework:spring-jdbc:$springJdbcVersion")
    compile("com.zaxxer:HikariCP:$hikariCpVersion")
    compile("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    compile("io.prometheus:simpleclient_common:$prometheusVersion")
    compile("khttp:khttp:$khttpVersion")
    compile("io.ktor:ktor-client-apache:$ktorVersion")
    compile("org.flywaydb:flyway-core:$flywayVersion")
    compile("no.nav.common:nais:$naisUtilsVersion")
    runtime("com.oracle.ojdbc:ojdbc8:$ojdbcVersion")

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
    maven("https://plugins.gradle.org/m2/")
    maven("https://dl.bintray.com/kotlin/ktor/")
    maven("http://repo.spring.io/plugins-release/")
    jcenter()
    mavenCentral()
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
    from(configurations.runtime.map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}
