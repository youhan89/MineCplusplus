import java.util.Properties

plugins {
    kotlin("jvm") version "1.8.0"
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "cubematic"

repositories {
    mavenCentral()
    maven(url="https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url="https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":paper"))
    api(project(":utils"))

    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("cubematic")
    }
}
kotlin {
    jvmToolchain(17)
}

tasks.register<Copy>("deploy") {
    //Check out local.properties.example
    val deployPath = Properties().let {
        it.load(project.rootProject.file("local.properties").inputStream())
        it.getProperty("deployTo", "./build")
    }

    dependsOn(tasks.getByName("shadowJar"))
    from(layout.buildDirectory.dir("libs/"))
    into(deployPath)
    include("*cubematic*$version*all.jar")
}