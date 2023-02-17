plugins {
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.github.johnrengelman.shadow") version "7.1.0"
    application
}

group = "com.github.doip-sim-ecu" // Change this to your organization
version = "0.1.0-SNAPSHOT"

repositories {
//    mavenLocal()
    mavenCentral()
//    maven("https://jitpack.io") for branches/snapshot versions
}

dependencies {
    implementation(kotlin("stdlib"))

    val ktorVersion = "2.2.3"
    // You should use the latest released stable version
    implementation("io.github.doip-sim-ecu:doip-sim-ecu-dsl:0.9.16")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:1.3.4")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
}

project.setProperty("mainClassName", "MainKt")

tasks {
    application {
        mainClass.set("MainKt")
    }
}

allOpen {
    annotation("kotlinx.serialization.Serializable")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.withType<JavaCompile>().configureEach {
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
}

