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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.11.0"
}

sourceSets.main {
    java.srcDirs(
        file("src"),
        file("gen")
    )
    resources.srcDir(file("resources"))
}

allprojects {
    apply {
        plugin("java")
    }

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

intellij {
    type.set(platformType)
    version.set(platformVersion)
    pluginName.set(pluginName_)
    downloadSources.set(platformDownloadSources)
    updateSinceUntilBuild.set(intellijUpdateSinceUntilBuild)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.addAll(*platformPlugins.split(',').map(String::trim).filter(String::isNotEmpty).toTypedArray())
}

tasks {

    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = sourceVersion
            apiVersion = kotlinApiVersion
            languageVersion = kotlinApiVersion
        }
    }

    runIde {
        jvmArgs = listOf(intellijJvmArgs)
    }

    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(pluginSinceBuild)
        untilBuild.set(pluginUntilBuild)
    }

    runPluginVerifier {
        ideVersions.addAll(pluginVerifierIdeVersions)
        externalPrefixes.addAll("com.intellij.javaee", "com.intellij.spring")
    }

    clean {
        doFirst {
            delete("out")
        }
    }
}

dependencies {

    implementation(kotlin("stdlib"))
    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("com.wutka:dtdparser:$dtdparserVersion")
    implementation("commons-io:commons-io:$commonsIOVersion")
    implementation("com.google.code.findbugs:jsr305:$findbugsVersion")
    implementation("org.apache.maven:maven-model:$mavenModelVersion")
    implementation("commons-codec:commons-codec:$commonsCodecVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
    implementation("org.apache.commons:commons-collections4:$commonsCollections4Version")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("com.sun.xml.bind:jaxb-impl:4.0.1")

    implementation("org.apache.solr:solr-solrj:$solrjVersion") {
        exclude("org.slf4j", "slf4j-api")
        exclude("org.apache.httpcomponents", "httpclient")
        exclude("org.apache.httpcomponents", "httpcore")
        exclude("org.apache.httpcomponents", "httpmime")
    }

    implementation(project(":rt-ant"))
}

