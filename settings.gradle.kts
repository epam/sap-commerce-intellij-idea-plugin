/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

rootProject.name = "SAP Commerce Developers Toolset"

plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.7.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()

        intellijPlatform {
            defaultRepositories()
        }
    }
}

include(":jps-plugin")

// auto-import sub-mobules
File(rootDir, "modules").walk()
    .maxDepth(4)
    .filter { it.isFile && it.name == "build.gradle.kts" }
    .map { it.parentFile.relativeTo(rootDir).path }
    .forEach { modulePath ->
        include(modulePath)
        project(":$modulePath").name = modulePath
            .replaceFirst("modules/", "")
            .replace(File.separatorChar, '-')
    }
