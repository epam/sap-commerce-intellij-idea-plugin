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

package sap.commerce.toolset.project.descriptor

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ProjectImportConstants
import java.io.File
import kotlin.io.path.exists

@Service
class ModuleRootResolver {

    fun isConfigModuleRoot(file: File) = with(file.toPath()) {
        resolve("licence").exists() && resolve("tomcat").resolve("tomcat_context.tpl").exists()
    }

    fun isCCv2ModuleRoot(file: File) =
        (file.absolutePath.contains(ProjectImportConstants.CCV2_CORE_CUSTOMIZE_NAME)
            || file.absolutePath.contains(ProjectImportConstants.CCV2_DATAHUB_NAME)
            || file.absolutePath.contains(ProjectImportConstants.CCV2_JS_STOREFRONT_NAME)
            )
            && File(file, ProjectImportConstants.CCV2_MANIFEST_NAME).isFile()

    fun isAngularModuleRoot(file: File) = Plugin.ANGULAR.ifActive { File(file, HybrisConstants.FILE_ANGULAR_JSON).isFile() }
        ?: false

    fun isPlatformModuleRoot(file: File) = file.getName() == EiConstants.Extension.PLATFORM
        && File(file, HybrisConstants.EXTENSIONS_XML).isFile()

    fun isHybrisExtensionRoot(file: File): Boolean = File(file, HybrisConstants.EXTENSION_INFO_XML).isFile

    fun isMavenModuleRoot(rootProjectDirectory: File) = Plugin.MAVEN.ifActive {
        if (rootProjectDirectory.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return@ifActive false
        }
        return@ifActive File(rootProjectDirectory, HybrisConstants.MAVEN_POM_XML).isFile()
    }
        ?: false

    fun isEclipseModuleRoot(rootProjectDirectory: File) = Plugin.ECLIPSE.ifActive {
        if (rootProjectDirectory.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return@ifActive false
        }
        return@ifActive File(rootProjectDirectory, HybrisConstants.DOT_PROJECT).isFile()
    }
        ?: false

    fun isGradleModuleRoot(file: File) = Plugin.GRADLE.ifActive {
        if (file.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return@ifActive false
        }
        return@ifActive File(file, HybrisConstants.GRADLE_SETTINGS).isFile()
            || File(file, HybrisConstants.GRADLE_BUILD).isFile()
    }
        ?: false

    fun isGradleKtsModuleRoot(file: File) = Plugin.GRADLE.ifActive {
        if (file.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return@ifActive false
        }
        return@ifActive File(file, HybrisConstants.GRADLE_SETTINGS_KTS).isFile()
            || File(file, HybrisConstants.GRADLE_BUILD_KTS).isFile()
    }
        ?: false

    companion object {
        fun getInstance(): ModuleRootResolver = application.service()
    }
}