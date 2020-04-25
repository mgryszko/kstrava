import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("com.github.ben-manes.versions") version "0.28.0"

    application
}

repositories {
    jcenter()
}

object Versions {
    val arrow = "0.10.5"
    val clickt = "2.5.0"
    val fuel = "2.2.0"
    val jackson = "2.10.2"
    val junit = "5.6.2"
    val atrium = "0.10.0"
    val wiremock = "2.26.3"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.github.kittinunf.fuel:fuel:${Versions.fuel}")
    implementation("com.github.kittinunf.fuel:fuel-jackson:${Versions.fuel}")
    implementation("com.fasterxml.jackson.core:jackson-core:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.github.ajalt:clikt:${Versions.clickt}")
    implementation("io.arrow-kt:arrow-fx:${Versions.arrow}")
    implementation("io.arrow-kt:arrow-syntax:${Versions.arrow}")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit}")
    testImplementation("ch.tutteli.atrium:atrium-fluent-en_GB:${Versions.atrium}")
    testImplementation("com.github.tomakehurst:wiremock-jre8:${Versions.wiremock}")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        lifecycle {
            events = setOf(FAILED, PASSED, SKIPPED)
            exceptionFormat = FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
            showStandardStreams = true
        }
    }
}

application {
    mainClassName = "com.grysz.kstrava.AppKt"
}
