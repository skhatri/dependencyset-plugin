plugins {
    id("idea")
    kotlin("jvm") version "1.5.21"

    signing

    id("maven-publish")

    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.14.0"
}

dependencies {
    implementation(gradleApi())
    testImplementation(gradleTestKit())

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.fasterxml.jackson.core:jackson-core:2.12.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.4")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.8")
}


val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}


artifacts {
    add("archives", sourcesJar)
    add("archives", javadocJar)
}


if (project.extra["target"] != "sonatype") {
    gradlePlugin {
        plugins {
            create("com.github.skhatri.dependencyset") {
                id = "com.github.skhatri.dependencyset"
                displayName = "A plugin to manage your dependency library version"
                description = "Manage versions of your libraries for java applications"
                implementationClass = "com.github.skhatri.dependency.DependencySetPlugin"
            }
        }
    }

    pluginBundle {
        website = "${project.extra["scm.url"]}"
        vcsUrl = "${project.extra["scm.url"]}"
        tags = listOf("spring", "spring-boot", "spark", "dependencies", "version")
    }
} else {
    publishing.repositories {
        maven {
            var uploadUrl: String = if (project.extra["release"] == "true") {
                "${project.extra["upload.release.url"]}"
            } else {
                "${project.extra["upload.snapshot.url"]}"
            }
            url = uri(uploadUrl)
            credentials {
                username = "${project.extra["upload.user"]}"
                password = "${project.extra["upload.password"]}"
            }
        }
    }
}

publishing.publications {
    create<MavenPublication>("pluginMaven") {
        artifact(sourcesJar.get())
        artifact(javadocJar.get())
    }
}

val scmUrl = project.extra["scm.url"]
project.publishing.publications.withType(MavenPublication::class.java).forEach { publication ->

    with(publication.pom) {
        withXml {
            val root = asNode()
            root.appendNode("name", project.name)
            root.appendNode("description", "A plugin to manage your dependency library version")
            root.appendNode("url", scmUrl)
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("${project.extra["author.handle"]}")
                name.set("${project.extra["author.name"]}")
                email.set("${project.extra["author.email"]}")
            }
        }
        scm {
            connection.set("scm:git:$scmUrl")
            developerConnection.set("scm:git:$scmUrl")
            url.set("${scmUrl}")
        }
    }
}

gradle.taskGraph.whenReady {
    if (allTasks.any { it is Sign }) {
        allprojects {
            extra["signing.keyId"] = "${project.extra["signing.keyId"]}"
            extra["signing.secretKeyRingFile"] = "${project.extra["signing.secretKeyRingFile"]}"
            extra["signing.password"] = "${project.extra["signing.password"]}"
        }
    }
}

signing {
    sign(publishing.publications["pluginMaven"])
}

tasks.withType<Sign>().configureEach {
    onlyIf { project.extra["release"] == "true" }
}

tasks.withType<org.gradle.api.tasks.Copy>().configureEach {
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.WARN
}