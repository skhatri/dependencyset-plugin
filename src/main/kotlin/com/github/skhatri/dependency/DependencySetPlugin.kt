package com.github.skhatri.dependency

import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.*
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.sonarqube.gradle.SonarQubeProperties

interface AppConfigExtension {
    val lang: ListProperty<String>
    val main: Property<String>
    val server: Property<String>
    val codeVersion: Property<String>
    val implementationItems: ListProperty<String>
    val testImplementationItems: ListProperty<String>

}

open class DependencySetPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.addTasks()
        project.configureCheckstyle()
        project.configureJacoco()
        project.configureSonar()

    }

}

internal fun Project.addTasks() = this.apply {
    val ext = project.extensions.create("appConfig", AppConfigExtension::class.java)
    val dependencyTask = project.tasks.register(
        "configureDependency",
        ConfigureDependencyTask::class.java,
        object : Action<ConfigureDependencyTask> {
            override fun execute(tsk: ConfigureDependencyTask) {
                tsk.main = ext.main.getOrElse("Application")
                tsk.codeVersion = ext.codeVersion.getOrElse("11")
                tsk.lang = ext.lang.getOrElse(listOf("java"))
                tsk.server = ext.server.getOrElse("undertow")
                tsk.implementationItems = ext.implementationItems.getOrElse(listOf())
                tsk.testImplementationItems = ext.testImplementationItems.getOrElse(listOf())
                tsk.description = "configure dependency set"
            }
        })

    listOf("java", "kotlin").forEach { lg ->
        val titleLang = lg.replaceFirstChar { c -> c.uppercaseChar() }
        project.getTasksByName("compile$titleLang", true).forEach { it.dependsOn(dependencyTask) }
    }
    project.getTasksByName("dependencyInsight", true).forEach { it.dependsOn(dependencyTask) }
    project.tasks.register("displaySet", DisplaySetTask::class.java, object : Action<DisplaySetTask> {
        override fun execute(ds: DisplaySetTask) {
            ds.description = "display available dependency set"
        }
    })
}

internal fun Project.configureCheckstyle() = this.apply {
    val configFile = file("$rootDir/gradle/settings/checkstyle.xml")
    if (project.plugins.hasPlugin("checkstyle") && configFile.exists()) {
        project.tasks.withType(Checkstyle::class.java).configureEach {
            it.ignoreFailures = false
            it.maxErrors = 0
            it.maxWarnings = 0

            it.configFile = configFile
            it.reports { r ->
                r.xml.isEnabled = false
                r.html.isEnabled = true
                r.html.stylesheet = resources.text.fromFile("$rootDir/gradle/settings/checkstyle.xsl")
            }
        }
        project.getTasksByName("build", true).forEach { b ->
            b.dependsOn(arrayOf("checkstyleMain", "checkstyleTest"))
        }
    }
}

internal fun Project.configureJacoco() = this.apply {
    if (project.plugins.hasPlugin("jacoco")) {
        project.tasks.withType(JacocoReport::class.java).configureEach {
            it.reports { r ->
                r.xml.isEnabled = true
                r.csv.isEnabled = false
                r.html.destination = file("${buildDir}/jacocoHtml")
            }
        }
        project.tasks.withType(JacocoCoverageVerification::class.java).configureEach {
            it.violationRules { v ->
                v.rule { vr ->
                    vr.isEnabled = true
                    vr.limit { vrl ->
                        vrl.minimum = "0.2".toBigDecimal()
                    }
                }

                v.rule { vr2 ->
                    vr2.isEnabled = false
                    vr2.element = "BUNDLE"
                    vr2.includes = listOf("**/*")
                    vr2.excludes = listOf("**/Application*")
                    vr2.limit { vr2l ->
                        vr2l.counter = "LINE"
                        vr2l.value = "COVEREDRATIO"
                        vr2l.minimum = "0.1".toBigDecimal()
                    }
                }

            }
        }
        val testTask = tasks.getByName("test")
        testTask.extensions.configure(JacocoTaskExtension::class.java) {
            it.destinationFile = file("$buildDir/jacoco/jacocoTest.exec")
            it.classDumpDir = file("$buildDir/jacoco/classpathdumps")
        }

        testTask.finalizedBy("jacocoTestReport")
        tasks.getByName("check").dependsOn(arrayOf("jacocoTestReport", "jacocoTestCoverageVerification"))
    }
}

internal fun Project.configureSonar() = this.apply {

    project.getTasksByName("sonarqube", true).forEach { task ->
        task.extensions.findByType(org.sonarqube.gradle.SonarQubeExtension::class.java)?.apply {
            properties(object : Action<SonarQubeProperties> {
                override fun execute(sonarProperties: SonarQubeProperties) {
                    sonarProperties.property("sonar.projectName", project.name)
                    sonarProperties.property(
                        "sonar.host.url",
                        project.extensions.extraProperties.has("sonar.host.url") ?: "http://localhost:9000"
                    )
                    sonarProperties.property("sonar.projectKey", project.name)
                    sonarProperties.property("sonar.projectVersion", "${project.version}")
                    sonarProperties.property("sonar.junit.reportPaths", "${projectDir}/build/test-results/test")
                    sonarProperties.property(
                        "sonar.coverage.jacoco.xmlReportPaths",
                        "${projectDir}/build/reports/jacoco/test/jacocoTestReport.xml"
                    )
                    sonarProperties.property("sonar.coverage.exclusions", "**/R.kt")
                    sonarProperties.property("sonar.language", "kotlin")
                }
            })
        }
    }
}


class DependencySet(lang: List<String>, server: String) {

    val dependencies =
        mapOf<String, List<String>>(
            "jackson" to listOf(
                "com.fasterxml.jackson.core:jackson-core:2.12.4",
                "com.fasterxml.jackson.core:jackson-databind:2.12.4",
                "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.4",
                "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.4",
            ),
            "jackson-kotlin" to listOf(
                "com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4"
            ),
            "junit" to listOf(
                "org.junit.jupiter:junit-jupiter-api:5.7.1",
                "org.junit.jupiter:junit-jupiter-engine:5.7.1",
                "org.junit.jupiter:junit-jupiter-params:5.7.1",
                "org.junit.vintage:junit-vintage-engine:5.7.1",

                "org.junit.platform:junit-platform-commons:1.7.1",
                "org.junit.platform:junit-platform-runner:1.7.1",
                "org.junit.platform:junit-platform-launcher:1.7.1",
                "org.junit.platform:junit-platform-engine:1.7.1",
                "org.mockito:mockito-core:3.3.3"
            ),
            "testcontainers" to listOf(
                "org.testcontainers:testcontainers:1.16.0",
                "org.testcontainers:junit-jupiter:1.16.0"
            ),
            "rx" to listOf(
                "io.projectreactor.addons:reactor-adapter:3.4.4"
            ),
            "rx-test" to listOf(
                "io.projectreactor:reactor-test:3.4.9",
            ),
            "kotlin" to listOf(
                "org.jetbrains.kotlin:kotlin-reflect:1.5.21",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.21",
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1",
                "org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.1"
            ),
            "grpc" to listOf(
                "com.google.protobuf:protobuf-java:3.12.2",
                "com.google.protobuf:protobuf-java-util:3.12.2",
                "io.grpc:grpc-protobuf:1.31.0",
                "io.grpc:grpc-stub:1.31.0",
                "io.grpc:grpc-services:1.31.0",
                "io.grpc:grpc-netty-shaded:1.31.0",
                "org.apache.tomcat:annotations-api:6.0.53",
            ),
            "spring-boot" to
                    listOf(
                        "spring-boot-starter-webflux",
                        "spring-boot-starter-$server",
                        "spring-boot-starter"
                    ).map { name ->
                        "org.springframework.boot:${name}:2.5.4"
                    },
            "spring-boot-test" to listOf(
                "org.springframework.boot:spring-boot-starter-test"
            ),
            "kafka" to listOf(
                "org.apache.kafka:kafka-clients:2.8.0",
                "org.apache.kafka:kafka-streams:2.8.0"
            )
        )

    fun byName(name: String): List<String> {
        return dependencies[name].orEmpty()
    }

}

abstract class DisplaySetTask : DefaultTask() {
    @TaskAction
    fun run() {
        logger.quiet("Available DependencySet values")
        logger.quiet("------------------------------")
        DependencySet(listOf(), "any").dependencies.keys.forEach { k ->
            logger.quiet(k)
        }
        logger.quiet("")
        logger.quiet("""
                Configure current build like so:
                    appConfig {
                        main.set("com.plugins.Application")
                        lang.value(listOf("java", "kotlin"))
                        implementationItems.value(listOf("spring-boot", "jackson", "coroutines", "kotlin"))
                        testImplementationItems.value(listOf("junit"))
                    }
                
            """.trimIndent())
    }
}

abstract class ConfigureDependencyTask : DefaultTask() {
    @get:Optional
    @get:Input
    abstract var lang: List<String>

    @get:Optional
    @get:Input
    abstract var main: String

    @get:Optional
    @get:Input
    abstract var codeVersion: String

    @get:Optional
    @get:Input
    abstract var server: String

    @get:Optional
    @get:Input
    abstract var implementationItems: List<String>

    @get:Optional
    @get:Input
    abstract var testImplementationItems: List<String>

    @TaskAction
    fun run() {
        if (project.pluginManager.hasPlugin("java")) {
            val javaPluginExt = project.extensions.getByType(JavaPluginExtension::class.java)
            javaPluginExt.run {
                sourceCompatibility = JavaVersion.valueOf("VERSION_$codeVersion")
                targetCompatibility = JavaVersion.valueOf("VERSION_$codeVersion")
            }
        }

        val depHandler = project.dependencies
        val resolver = DependencySet(lang, server)

        fun addDep(pair: Pair<String, String>) {
            resolver.byName(pair.first).forEach { line ->
                val parts = line.split(':')
                val version: String? = if (parts.size > 2) parts[2] else null
                if (project.logger.isInfoEnabled) {
                    project.logger.info("resolving configuration ${pair.second}, dependency: ${parts[0]}:${parts[1]}:${version}")
                }
                depHandler.add(pair.second, DefaultExternalModuleDependency(parts[0], parts[1], version))
            }
        }

        implementationItems.forEach { item ->
            val langConfig = lang.flatMap { l -> listOf("$l" to "implementation", "$item-$l" to "implementation") }
            val pairOfConfig = listOf(
                item to "implementation", "$item-test" to "testImplementation",
            ) + langConfig
            pairOfConfig.forEach { pair ->
                addDep(pair)
            }
        }

        testImplementationItems.forEach { item ->
            addDep(item to "testImplementation")
        }
        if (testImplementationItems.contains("junit")) {
            project.tasks.withType(Test::class.java).configureEach {
                it.useJUnitPlatform()
            }
        }
    }

}
