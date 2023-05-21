plugins {
    `java-library`
    `maven-publish`
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"
val artifactName = "items"

dependencies {
    compileOnly(commonLibs.acf)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.protocollib)
    compileOnly(commonLibs.jda)
    compileOnly(commonLibs.nbtapi)
    compileOnly(commonLibs.craftbukkit)
    compileOnly(commonLibs.taskchain)
    compileOnly(commonLibs.mythicmobs)
    compileOnly(commonLibs.springdatamongodb)
    compileOnly(commonLibs.mongodbdrivercore)
    compileOnly(commonLibs.mongodbdriversync)
    compileOnly(commonLibs.jedis)
    compileOnly(project(":Projects:Database"))
    compileOnly(project(":Projects:Common"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.runicrealms.plugin"
            artifactId = artifactName
            version = "1.0-SNAPSHOT"
            from(components["java"])
        }
    }
}