plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("com.github.ben-manes.versions") version "0.28.0"

    application
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.core:jackson-core:2.10.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
    implementation("com.github.ajalt:clikt:2.5.0")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

application {
    mainClassName = "com.grysz.kstrava.AppKt"
}
