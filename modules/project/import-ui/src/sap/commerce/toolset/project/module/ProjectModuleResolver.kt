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

package sap.commerce.toolset.project.module

import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.project.ProjectConstants
import java.io.File
import kotlin.io.path.exists

object ProjectModuleResolver {

    fun isConfigModule(file: File) = with(file.toPath()) {
        resolve("licence").exists() && resolve("tomcat").resolve("tomcat_context.tpl").exists()
    }

    fun isCCv2Module(file: File) =
        (file.absolutePath.contains(CCv2Constants.CORE_CUSTOMIZE_NAME)
            || file.absolutePath.contains(CCv2Constants.DATAHUB_NAME)
            || file.absolutePath.contains(CCv2Constants.JS_STOREFRONT_NAME)
            )
            && File(file, CCv2Constants.MANIFEST_NAME).isFile()

    fun isAngularModule(file: File) = Plugin.ANGULAR.ifActive { File(file, HybrisConstants.FILE_ANGULAR_JSON).isFile() }
        ?: false

    fun isPlatformModule(file: File) = file.getName() == ProjectConstants.Extension.PLATFORM
        && File(file, HybrisConstants.EXTENSIONS_XML).isFile()

    fun isHybrisExtension(file: File): Boolean = File(file, HybrisConstants.EXTENSION_INFO_XML).isFile

    fun isMavenModule(rootProjectDirectory: File) = Plugin.MAVEN.ifActive {
        if (rootProjectDirectory.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return@ifActive false
        }
        return@ifActive File(rootProjectDirectory, HybrisConstants.MAVEN_POM_XML).isFile()
    }
        ?: false

    fun isEclipseModule(rootProjectDirectory: File) = Plugin.ECLIPSE.ifActive {
        if (rootProjectDirectory.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return@ifActive false
        }
        return@ifActive File(rootProjectDirectory, HybrisConstants.DOT_PROJECT).isFile()
    }
        ?: false

    fun isGradleModule(file: File) = Plugin.GRADLE.ifActive {
        if (file.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return@ifActive false
        }
        return@ifActive File(file, HybrisConstants.GRADLE_SETTINGS).isFile()
            || File(file, HybrisConstants.GRADLE_BUILD).isFile()
    }
        ?: false

    fun isGradleKtsModule(file: File) = Plugin.GRADLE.ifActive {
        if (file.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return@ifActive false
        }
        return@ifActive File(file, HybrisConstants.GRADLE_SETTINGS_KTS).isFile()
            || File(file, HybrisConstants.GRADLE_BUILD_KTS).isFile()
    }
        ?: false

}