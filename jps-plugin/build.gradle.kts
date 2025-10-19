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

fun properties(key: String) = providers.gradleProperty(key)

plugins {
    id("java")
    id("org.jetbrains.intellij.platform.module")
    alias(libs.plugins.kotlin) // Kotlin support
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
        vendor = JvmVendorSpec.JETBRAINS
    }
}

kotlin {
    jvmToolchain(8)
}

sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("resources")
    }
}

intellijPlatform {
    buildSearchableOptions = true
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate(properties("intellij.version")) {
            useInstaller = false
        }
        bundledPlugins(
            "com.intellij.java",
        )
    }
}
