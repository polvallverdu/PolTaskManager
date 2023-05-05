import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    apply { `maven-publish` }
    apply { java }
}

group = "dev.polv.taskmanager"
version = properties["version"].toString()

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.register("sourcesJar", Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.polv.taskmanager"
            artifactId = "PolTaskManager"
            version = properties["version"].toString()

            from(components["java"])
            artifact(tasks["sourcesJar"]) {
                classifier = "sources"
            }
        }
    }
    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

tasks.named("publish") {
    dependsOn("publishToMavenLocal")
}

/*signing {
    sign(publishing.publications["maven"])
}*/

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}