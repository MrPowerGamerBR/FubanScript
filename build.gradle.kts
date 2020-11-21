import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    antlr
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    antlr("org.antlr:antlr4:4.8-1") // use ANTLR version 4
    implementation("org.antlr:antlr4-runtime:4.8-1")
    implementation(kotlin("reflect"))
    api("io.github.microutils:kotlin-logging:2.0.3")
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-long-messages")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("FubanScript-shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "br.pucsp.fubanscript.FubanScriptLauncher"))
        }
    }
}