/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java") // Java support
    id("idea") // IDEA support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle IntelliJ Plugin
    alias(libs.plugins.openAPIGenerator) // openapi Generator
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
        jetbrainsRuntime()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.JETBRAINS
    }
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.JETBRAINS
    }
}

sourceSets {
    main {
        java.srcDirs("src", "gen", "ccv2")
        resources.srcDirs("resources")
    }
    test {
        java.srcDirs("tests")
    }
}

idea {
    module {
        generatedSourceDirs.add(file("gen"))
        generatedSourceDirs.add(file("ccv2"))
    }
}

changelog {
    version = properties("intellij.plugin.version")
    groups = listOf()
    headerParserRegex = """(\d+(.\d+)*)""".toRegex()
}

val projectJvmArguments = mutableListOf<String>().apply {
    add(properties("intellij.jvm.args").get())

    if (OperatingSystem.current().isMacOsX) {
        add("-Xdock:name=${project.name}")
        // converted via ImageMagick, https://gist.github.com/plroebuck/af19a26c908838c7f9e363c571199deb
        add("-Xdock:icon=${project.rootDir}/macOS_dockIcon.icns")
    }
}

// OpenAPI - Gradle plugin
// https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
// OpenAPI - Kotlin generator
// https://openapi-generator.tech/docs/generators/kotlin/
val ccv2OpenApiSpecs = listOf(
    Triple("ccv1OpenApiGenerate", "commerce-cloud-management-api-v1.yaml", "com.intellij.idea.plugin.hybris.ccv1"),
    Triple("ccv2OpenApiGenerate", "commerce-cloud-management-api-v2.yaml", "com.intellij.idea.plugin.hybris.ccv2"),
)
val ccv2OpenApiTasks = ccv2OpenApiSpecs.mapIndexed { index, (taskName, schema, packagePrefix) ->
    tasks.register<GenerateTask>(taskName) {
        group = "openapi tools"
        generatorName.set("kotlin")

        inputSpec.set("$rootDir/resources/specs/$schema")
        outputDir.set("$rootDir/ccv2")

        // Custom template required to enable request-specific headers for Authentication
        // https://openapi-generator.tech/docs/templating
        // https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator/src/main/resources/kotlin-client/libraries/jvm-okhttp
        templateDir.set("$rootDir/resources/openapi/templates")

        apiPackage.set("$packagePrefix.api")
        packageName.set("$packagePrefix.invoker")
        modelPackage.set("$packagePrefix.model")

        cleanupOutput.set(index == 0)
        skipOperationExample.set(true)
        generateApiDocumentation.set(false)
        generateApiTests.set(false)
        generateModelTests.set(false)

        globalProperties.set(
            mapOf(
                "modelDocs" to "false",
            )
        )
        configOptions.set(
            mapOf(
                "useSettingsGradle" to "false",
                "omitGradlePluginVersions" to "true",
                "omitGradleWrapper" to "true",
                "useCoroutines" to "true",
                "sourceFolder" to "",
            )
        )

        if (index > 0) {
            val previousTaskName = ccv2OpenApiSpecs[index - 1].first
            dependsOn(previousTaskName)
        } else {
            dependsOn("copyChangelog")
        }
    }
}

intellijPlatform {
    buildSearchableOptions = true

    pluginConfiguration {
        id = properties("intellij.plugin.id")
        name = properties("intellij.plugin.name")
        version = properties("intellij.plugin.version")
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }
        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = with(changelog) {
            getAll().values
                .take(3)
                .joinToString("") {
                    renderItem(
                        it
                            .withLinks(true)
                            .withEmptySections(false),
                        Changelog.OutputType.HTML,
                    )
                }
        }

        ideaVersion {
            sinceBuild = properties("intellij.plugin.since.build")
            untilBuild = properties("intellij.plugin.until.build")
        }

        vendor {
            name = "EPAM_Systems"
            email = "hybrisideaplugin@epam.com"
            url = "https://github.com/epam/sap-commerce-intellij-idea-plugin"
        }

    }

    pluginVerification {
        freeArgs = listOf("-mute", "TemplateWordInPluginId,TemplateWordInPluginName,ForbiddenPluginIdPrefix")

        ides {
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaUltimate, IntelliJPlatformType.IntellijIdeaCommunity)
                channels = listOf(ProductRelease.Channel.EAP, ProductRelease.Channel.RELEASE)
                sinceBuild = properties("intellij.plugin.since.build")
                untilBuild = properties("intellij.plugin.until.build")
            }
        }
    }
}

tasks {
    val copyChangelog by registering(Copy::class) {
        from("CHANGELOG.md")
        into("resources")
    }

    patchPluginXml {
        dependsOn(copyChangelog)
    }

    wrapper {
        gradleVersion = properties("gradle.version").get()
    }

    runIde {
        jvmArgs = projectJvmArguments
        maxHeapSize = properties("intellij.maxHeapSize").get()

        applyRunIdeSystemSettings()
    }

    clean {
        doFirst {
            delete("out")
        }
    }

    test {
        useJUnitPlatform()
    }

    compileJava {
        dependsOn(ccv2OpenApiTasks)
    }

    compileKotlin {
        dependsOn(ccv2OpenApiTasks)
    }

    printProductsReleases {
        channels = listOf(ProductRelease.Channel.EAP)
        types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
        untilBuild = provider { null }

        doLast {
            productsReleases.get().max()
        }
    }

    buildSearchableOptions {
        var originalDisabledPluginsContent = ""

        doFirst {
            // There is an issue with JRebelPlugin which blocks creation of the searchable options
            sandboxConfigDirectory.file("disabled_plugins.txt").get().asFile
                .also { originalDisabledPluginsContent = it.readText() }
                .writeText("JRebelPlugin")
        }

        doLast {
            sandboxConfigDirectory.file("disabled_plugins.txt").get().asFile
                .writeText(originalDisabledPluginsContent)
        }
    }
}

intellijPlatformTesting {
    runIde {
        val runIdeCommunity by registering {
            type = IntelliJPlatformType.IntellijIdeaCommunity
            version = properties("intellij.version")

            task {
                jvmArgs = projectJvmArguments
                maxHeapSize = properties("intellij.maxHeapSize").get()

                applyRunIdeSystemSettings()
            }
        }
    }
}

// does not work well, especially in the case of Maps
//tasks.named<RunIdeTask>("runIde") {
//    jvmArgumentProviders += CommandLineArgumentProvider {
//        listOf("-Didea.kotlin.plugin.use.k2=true")
//    }
//}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    implementation(libs.bundles.openapi)
    implementation(libs.bundles.commons)
    implementation(libs.bundles.jaxb)
    implementation(libs.jsr305)
    implementation(libs.jsoup)
    implementation(libs.dtdparser)
    implementation(libs.maven.model)
    implementation(libs.solr.solrj) {
        exclude("org.slf4j", "slf4j-api")
        exclude("org.apache.httpcomponents", "httpclient")
        exclude("org.apache.httpcomponents", "httpcore")
        exclude("org.apache.httpcomponents", "httpmime")
    }
    testImplementation(kotlin("test"))
    testRuntimeOnly("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdeaUltimate(properties("intellij.version"), useInstaller = false)

        pluginVerifier()

        // printBundledPlugins for bundled plugins
        bundledPlugins(
            "com.intellij.java",
            "com.intellij.java-i18n",
            "com.intellij.cron",
            "org.intellij.intelliLang",
            "com.intellij.copyright",
            "com.intellij.database",
            "com.intellij.diagram",
            "com.intellij.spring",
            "com.intellij.properties",
            "org.intellij.groovy",
            "com.intellij.gradle",
            "com.intellij.javaee",
            "com.intellij.javaee.el",
            "com.intellij.javaee.web",
            "com.intellij.platform.images",
            "com.intellij.modules.json",
            "org.jetbrains.idea.maven",
            "org.jetbrains.idea.maven.model",
            "org.jetbrains.idea.maven.server.api",
            "org.jetbrains.idea.eclipse",
            "org.jetbrains.kotlin",
            "org.jetbrains.plugins.terminal",
            "JavaScript",
            "JUnit",
        )

        // https://plugins.jetbrains.com/intellij-platform-explorer/extensions

        compatiblePlugins(
            "AntSupport",                       // Ant                  https://plugins.jetbrains.com/plugin/23025-ant
            "PsiViewer",                        // PsiViewer            https://plugins.jetbrains.com/plugin/227-psiviewer
            "JRebelPlugin",                     // JRebel and XRebel    https://plugins.jetbrains.com/plugin/4441-jrebel-and-xrebel
            "AngularJS"                         // Angular              https://plugins.jetbrains.com/plugin/6971-angular
        )

        // Big Data Tools:
        // incredibly sad, but as for now API cannot be used by 3rd-party plugins
        // https://plugins.jetbrains.com/bundles/8-big-data-tools
//        plugins(
//            // https://plugins.jetbrains.com/plugin/12494-big-data-tools
//            "com.intellij.bigdatatools:241.14494.158",
//            // https://plugins.jetbrains.com/plugin/21713-big-data-tools-core
//            "com.intellij.bigdatatools.core:241.14494.158",
//            // https://plugins.jetbrains.com/plugin/21701-big-data-file-viewer
//            "com.intellij.bigdatatools.binary.files:241.14494.158",
//            // https://plugins.jetbrains.com/plugin/21712-metastore-core
//            "com.intellij.bigdatatools.metastore.core:241.14494.158",
//            // https://plugins.jetbrains.com/plugin/21700-spark/versions
//            "com.intellij.bigdatatools.spark:241.14494.158",
//            // https://plugins.jetbrains.com/plugin/21673-zeppelin
//            "com.intellij.bigdatatools.zeppelin:241.14494.158",
//            // https://plugins.jetbrains.com/plugin/21702-flink
//            "com.intellij.bigdatatools.flink:241.14494.158",
//            // https://plugins.jetbrains.com/plugin/21704-kafka
//            "com.intellij.bigdatatools.kafka:241.14494.158",
//            // https://plugins.jetbrains.com/plugin/21706-remote-file-systems
//            "com.intellij.bigdatatools.rfs:241.15989.150",
//        )
    }
}

fun RunIdeTask.applyRunIdeSystemSettings() {
    systemProperty("ide.experimental.ui", "true")
    systemProperty("ide.show.tips.on.startup.default.value", false)
    systemProperty("idea.trust.all.projects", true)
    systemProperty("jb.consents.confirmation.enabled", false)
}
