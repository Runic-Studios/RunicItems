val artifactName = "items"
val rrGroup: String by rootProject.extra
val rrVersion: String by rootProject.extra

plugins {
    `java-library`
    `maven-publish`
}

group = rrGroup
version = rrVersion

dependencies {
    compileOnly(commonLibs.acf)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.protocollib)
    compileOnly(commonLibs.jda)
    compileOnly(commonLibs.nbtapi)
    compileOnly(commonLibs.taskchain)
    compileOnly(commonLibs.mythicmobs)
    compileOnly(commonLibs.springdatamongodb)
    compileOnly(commonLibs.mongodbdrivercore)
    compileOnly(commonLibs.mongodbdriversync)
    compileOnly(commonLibs.jedis)
    compileOnly(commonLibs.luckperms)
    compileOnly(project(":Projects:Database"))
    compileOnly(project(":Projects:Common"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rrGroup
            artifactId = artifactName
            version = rrVersion
            from(components["java"])
        }
    }
}