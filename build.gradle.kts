import java.util.Properties

plugins {
    kotlin("jvm") version "1.8.0"
    `java-library`
}

group = "cubematic"

repositories {
    mavenCentral()
    maven(url="https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url="https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
    //compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
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
    include("*fat*$version*.jar")
}