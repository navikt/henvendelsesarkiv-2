val junitJupiterVersion = "5.2.0"
val spekVersion = "1.1.5"
val hsqlDbVersion = "2.4.1"
val kluentVersion = "1.38"
val khttpVersion = "0.1.0"
val javalinVersion = "2.0.0.RC3"
val slf4jVersion = "1.8.0-beta2"
val jacksonVersion = "2.9.6"
val springJdbcVersion = "5.0.8.RELEASE"
val hikariCpVersion = "2.7.8"

val mainClass = "no.nav.henvendelsesarkiv.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.2.60"
}

buildscript {
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

application {
    mainClassName = mainClass
}

dependencies {
    compile(kotlin("stdlib"))
    compile("io.javalin:javalin:$javalinVersion")
    compile("org.slf4j:slf4j-simple:$slf4jVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    compile("org.springframework:spring-jdbc:$springJdbcVersion")
    compile("com.zaxxer:HikariCP:$hikariCpVersion")

    testCompile("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testCompile("org.amshove.kluent:kluent:$kluentVersion")
    testCompile("khttp:khttp:$khttpVersion")
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
    jcenter()
    mavenCentral()
    maven("https://repo.adeo.no/repository/maven-releases/")
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
