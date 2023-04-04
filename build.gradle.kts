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

plugins {
    idea
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("org.jetbrains.intellij") version "1.13.3"
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
    type.set(properties("intellij.type"))
    version.set(properties("intellij.version"))
    pluginName.set(properties("intellij.plugin.name"))
    downloadSources.set(properties("intellij.download.sources").get().toBoolean())
    updateSinceUntilBuild.set(properties("intellij.update.since.until.build").get().toBoolean())

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.addAll(*properties("intellij.plugins").get().split(',').map(String::trim).filter(String::isNotEmpty).toTypedArray())
}

sourceSets.main {
    java.srcDirs(
        file("src"),
        file("gen")
    )
    resources.srcDir(file("resources"))
}

tasks {

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
        version.set(properties("intellij.plugin.version"))
        sinceBuild.set(properties("intellij.plugin.since.build"))
        untilBuild.set(properties("intellij.plugin.until.build"))
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

dependencies {

    implementation(kotlin("stdlib"))
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("com.wutka:dtdparser:1.21")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.apache.maven:maven-model:3.8.7")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("com.sun.xml.bind:jaxb-impl:4.0.1")

    implementation("org.apache.solr:solr-solrj:8.8.2") {
        exclude("org.slf4j", "slf4j-api")
        exclude("org.apache.httpcomponents", "httpclient")
        exclude("org.apache.httpcomponents", "httpcore")
        exclude("org.apache.httpcomponents", "httpmime")
    }
}

