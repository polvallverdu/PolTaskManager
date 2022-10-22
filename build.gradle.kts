import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    `maven-publish`
    `java-library`
    signing
    application
}

group = "engineer.pol.taskmanager"
version = properties["version"].toString()

repositories {
    mavenCentral()
    //maven("https://maven.fabricmc.net")
}

dependencies {
    testImplementation(kotlin("test"))
    //compileOnly("net.fabricmc.fabric-api:fabric-api:0.63.0+1.19.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}

publishing {
    println(properties["polRegistryUsername"])
    repositories {
        maven {
            name = "polRegistry"
            url = uri("https://artifactory.wosher.co/artifactory/pol-public/")
            credentials {
                username = "${properties["polRegistryUsername"]}"
                password = "${properties["polRegistryPassword"]}"
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "engineer.pol.taskmanager"
            artifactId = "PolTaskManager"
            version = properties["version"].toString()

            from(components["java"])
        }
    }
}

/*signing {
    sign(publishing.publications["maven"])
}*/

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}