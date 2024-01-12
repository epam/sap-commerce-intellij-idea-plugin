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

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java") // Java support
    id("idea") // IDEA support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle IntelliJ Plugin
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

intellij {
    type = properties("intellij.type")
    version = properties("intellij.version")
    pluginName = properties("intellij.plugin.name")
    downloadSources = properties("intellij.download.sources").get().toBoolean()
    updateSinceUntilBuild = properties("intellij.update.since.until.build").get().toBoolean()

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("intellij.plugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

sourceSets {
    main {
        java.srcDirs("src", "gen")
        resources.srcDirs("resources")
    }
    test {
        java.srcDirs("tests")
    }
}

idea {
    module {
        generatedSourceDirs.add(file("gen"))
    }
}

changelog {
    version = properties("intellij.plugin.version")
    groups = listOf()
    headerParserRegex = """(\d+(.\d+)*)""".toRegex()
}

tasks {
    wrapper {
        gradleVersion = properties("gradle.version").get()
    }

    runIde {
        jvmArgs = mutableListOf<String>().apply {
            add(properties("intellij.jvm.args").get())

            if (OperatingSystem.current().isMacOsX) {
                add("-Xdock:name=SAP-Commerce-Developers-Toolset")
                // converted via ImageMagick, https://gist.github.com/plroebuck/af19a26c908838c7f9e363c571199deb
                add("-Xdock:icon=${project.rootDir}/macOS_dockIcon.icns")
            }
        }
        maxHeapSize = "3g"
        /*
        To be able to start IDEA Community edition one has to uncomment `ideDir` property and specify an absolute path to the Application
        references:
         - issue > https://github.com/JetBrains/gradle-intellij-plugin/issues/578
         - docs > https://plugins.jetbrains.com/docs/intellij/dev-alternate-products.html#configuring-gradle-build-script-using-the-intellij-idea-product-attribute
         */
//        ideDir = file("/Users/<user>/Applications/IntelliJ IDEA Community Edition.app/Contents")
    }

    patchPluginXml {
        version = properties("intellij.plugin.version")
        sinceBuild = properties("intellij.plugin.since.build")
        untilBuild = properties("intellij.plugin.until.build")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
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
    }

    runPluginVerifier {
        ideVersions.add(properties("plugin.verifier.ide.versions"))
    }

    clean {
        doFirst {
            delete("out")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {

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
}
