import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.ir.backend.js.compile
import java.util.Properties

plugins {
    kotlin("jvm") version "1.8.0"
    `java-library`
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url="https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks.register<Copy>("deploy") {
    //Check out local.properties.example
    val deployPath = Properties().let {
        it.load(project.rootProject.file("local.properties").inputStream())
        it.getProperty("deployTo", "./build")
    }

    dependsOn(tasks.getByName("fatJar"))
    from(layout.buildDirectory.dir("libs/"))
    into(deployPath)
    include("*fat*.jar")
}