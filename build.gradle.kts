/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
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

// OpenAPI - Gradle plugin
// https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
// OpenAPI - Kotlin generator
// https://openapi-generator.tech/docs/generators/kotlin/
openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/resources/specs/commerce-cloud-management-api.yaml")
    outputDir.set("$rootDir/ccv2")

    apiPackage.set("com.intellij.idea.plugin.hybris.ccv2.api")
    packageName.set("com.intellij.idea.plugin.hybris.ccv2.invoker")
    modelPackage.set("com.intellij.idea.plugin.hybris.ccv2.model")

    skipOperationExample.set(true)
    cleanupOutput.set(true)
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
}

intellijPlatform {
    pluginConfiguration {
        id = "com.intellij.idea.plugin.sap.commerce"
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

    verifyPlugin {
        ides {
            ide(properties("plugin.verifier.ide.versions"))
            recommended()
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaUltimate)
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = properties("intellij.plugin.since.build")
                untilBuild = properties("intellij.plugin.until.build")
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradle.version").get()
    }

    runIde {
        jvmArgs = mutableListOf<String>().apply {
            add(properties("intellij.jvm.args").get())

            if (OperatingSystem.current().isMacOsX) {
                add("-Xdock:name=${project.name}")
                // converted via ImageMagick, https://gist.github.com/plroebuck/af19a26c908838c7f9e363c571199deb
                add("-Xdock:icon=${project.rootDir}/macOS_dockIcon.icns")
            }
        }
        maxHeapSize = "3g"
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
        dependsOn(openApiGenerate)
    }

    compileKotlin {
        dependsOn(openApiGenerate)
    }

    printProductsReleases {
        channels = listOf(ProductRelease.Channel.EAP)
        types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
        untilBuild = provider { null }

        doLast {
            productsReleases.get().max()
        }
    }
}

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

    intellijPlatform {
        intellijIdeaUltimate(properties("intellij.version"))

        instrumentationTools()
        pluginVerifier()
//        testFramework(TestFrameworkType.Platform.JUnit4)

        // printBundledPlugins for bundled plugins
        bundledPlugin("com.intellij.java")
        bundledPlugin("com.intellij.java-i18n")
        bundledPlugin("org.intellij.intelliLang")
        bundledPlugin("com.intellij.copyright")
        bundledPlugin("com.intellij.database")
        bundledPlugin("com.intellij.diagram")
        bundledPlugin("com.intellij.spring")
        bundledPlugin("com.intellij.properties")
        bundledPlugin("org.intellij.groovy")
        bundledPlugin("com.intellij.gradle")
        bundledPlugin("com.intellij.javaee")
        bundledPlugin("com.intellij.javaee.el")
        bundledPlugin("com.intellij.javaee.web")
        bundledPlugin("com.intellij.platform.images")
        bundledPlugin("org.jetbrains.idea.maven")
        bundledPlugin("org.jetbrains.idea.maven.model")
        bundledPlugin("org.jetbrains.idea.maven.server.api")
        bundledPlugin("org.jetbrains.idea.eclipse")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("JavaScript")
        bundledPlugin("JUnit")

        // https://plugins.jetbrains.com/intellij-platform-explorer/extensions
        plugin("AntSupport:241.14494.158")
        plugin("PsiViewer:241.14494.158-EAP-SNAPSHOT")
        plugin("JRebelPlugin:2024.2.0")
    }
}
