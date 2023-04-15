/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2020 EPAM Systems <hybrisideaplugin@epam.com>
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

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
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
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath = provider { file(".qodana").canonicalPath }
    reportPath = provider { file("build/reports/inspections").canonicalPath }
    saveReport = true
    showReport = environment("QODANA_SHOW_REPORT").map { it.toBoolean() }.getOrElse(false)
}

sourceSets.main {
    java.srcDirs(
        file("src"),
        file("gen")
    )
    resources.srcDir(file("resources"))
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

//    setupDependencies {
//        doLast {
//            // Fixes IDEA-298989.
//            fileTree("$buildDir/instrumented/instrumentCode") { include("**/*Form.class") }.files.forEach { delete(it) }
//        }
//    }
//
//    // TODO: remove before final commit
//    buildSearchableOptions {
//        enabled = false
//    }

    runIde {
        jvmArgs = listOf(properties("intellij.jvm.args").get())
        maxHeapSize = "3g"
    }

    patchPluginXml {
        version = properties("intellij.plugin.version")
        sinceBuild = properties("intellij.plugin.since.build")
        untilBuild = properties("intellij.plugin.until.build")
    }

    runPluginVerifier {
        ideVersions.add(properties("plugin.verifier.ide.versions"))
    }

    clean {
        doFirst {
            delete("out")
        }
    }

//    runInspections {
////        env("QODANA_REPO_URL", "https://github.com/epam/sap-commerce-intellij-idea-plugin")
//        mount("~/.m2", "/root/.m2")
//    }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {

    implementation(libs.commons.collections4)
    implementation(libs.commons.io)
    implementation(libs.commons.codec)
    implementation(libs.jaxb.impl)
    implementation(libs.jakarta.xml.bind.api)
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

}

