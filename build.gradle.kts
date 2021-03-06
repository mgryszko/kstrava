import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    id("com.github.ben-manes.versions") version "0.28.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"

    application
}

repositories {
    jcenter()
    mavenCentral()
}

object Versions {
    const val arrow = "0.11.0"
    const val clickt = "2.5.0"
    const val fuel = "2.2.0"
    const val jackson = "2.10.2"
    const val junit = "5.6.2"
    const val atrium = "0.10.0"
    const val mockk = "1.10.0"
    const val wiremock = "2.26.3"
    const val slf4j = "1.7.30"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("com.github.kittinunf.fuel:fuel:${Versions.fuel}")
    implementation("com.github.kittinunf.fuel:fuel-jackson:${Versions.fuel}")
    implementation("com.fasterxml.jackson.core:jackson-core:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.github.ajalt:clikt:${Versions.clickt}")
    implementation(platform("io.arrow-kt:arrow-stack:${Versions.arrow}"))
    implementation("io.arrow-kt:arrow-fx")
    implementation("io.arrow-kt:arrow-mtl")
    implementation("io.arrow-kt:arrow-fx-mtl")
    implementation("io.arrow-kt:arrow-syntax")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit}")
    testImplementation("ch.tutteli.atrium:atrium-fluent-en_GB:${Versions.atrium}")
    testImplementation("com.github.tomakehurst:wiremock-jre8:${Versions.wiremock}")
    testImplementation("org.slf4j:slf4j-nop:${Versions.slf4j}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
}

val kotlinOptions: KotlinJvmOptions.() -> Unit = {
    jvmTarget = "11"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions(kotlinOptions)
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions(kotlinOptions)

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
