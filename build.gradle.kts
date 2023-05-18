plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://jcenter.bintray.com")
}

dependencies {
    compileOnly(commonLibs.mythicmobs)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.protocollib)
    compileOnly(project(":Projects:Core"))
    implementation("net.dv8tion.JDA.4.2.0_229")
    compileOnly(commonLibs.nbtapi)
    compileOnly(commonLibs.craftbukkit)
    compileOnly(project(":Projects:Guilds"))
    compileOnly(projects(":Projects:Bank"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.runicrealms.plugin"
            artifactId = "items"
            version = "1.0-SNAPSHOT"
            from(components["java"])
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
//    build {
//        dependsOn(shadowJar)
//    }
}

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")