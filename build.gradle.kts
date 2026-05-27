plugins {
    kotlin("jvm") version "1.9.24"
    id("org.gradle.application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    val javafxVersion = "21"
    implementation("org.openjfx:javafx-controls:$javafxVersion:linux")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:linux")
    implementation("org.openjfx:javafx-base:$javafxVersion:linux")

    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

configure<org.gradle.api.plugins.JavaApplication> {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    tasks.named<JavaExec>("run") {
        standardInput = System.`in`
    }
}