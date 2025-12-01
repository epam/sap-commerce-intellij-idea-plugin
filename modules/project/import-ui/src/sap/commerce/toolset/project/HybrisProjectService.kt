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

package sap.commerce.toolset.project

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import org.jetbrains.idea.maven.model.MavenConstants
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.project.ProjectUtil.isHybrisModuleRoot
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.vfs.VirtualFileSystemService
import java.io.File
import kotlin.io.path.exists

@Service
class HybrisProjectService {

    fun isConfigModule(file: File) = with(file.toPath()) {
        resolve("licence").exists() && resolve("tomcat").resolve("tomcat_context.tpl").exists()
    }

    fun isCCv2Module(file: File): Boolean {
        return (file.absolutePath.contains(CCv2Constants.CORE_CUSTOMIZE_NAME)
            || file.absolutePath.contains(CCv2Constants.DATAHUB_NAME)
            || file.absolutePath.contains(CCv2Constants.JS_STOREFRONT_NAME)
            )
            && File(file, CCv2Constants.MANIFEST_NAME).isFile()
    }

    fun isAngularModule(file: File) = File(file, HybrisConstants.FILE_ANGULAR_JSON).isFile()

    fun isPlatformModule(file: File) = file.getName() == HybrisConstants.EXTENSION_NAME_PLATFORM
        && File(file, HybrisConstants.EXTENSIONS_XML).isFile()

    fun isPlatformExtModule(file: File) = file.absolutePath.contains(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX)
        && File(file, HybrisConstants.EXTENSION_INFO_XML).isFile()
        && !isCoreExtModule(file)

    fun isCoreExtModule(file: File) = file.absolutePath.contains(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX)
        && file.getName() == HybrisConstants.EXTENSION_NAME_CORE
        && File(file, HybrisConstants.EXTENSION_INFO_XML).isFile()

    fun isHybrisModule(file: File): Boolean = isHybrisModuleRoot(file)

    fun isOutOfTheBoxModule(file: File, rootProjectDescriptor: HybrisProjectDescriptor): Boolean {
        val extDir = rootProjectDescriptor.externalExtensionsDirectory
        if (extDir != null) {
            if (VirtualFileSystemService.getInstance().fileContainsAnother(extDir, file)) {
                // this will override bin/ext-* naming convention.
                return false
            }
        }
        return (file.absolutePath.contains(HybrisConstants.PLATFORM_OOTB_MODULE_PREFIX) ||
            file.absolutePath.contains(HybrisConstants.PLATFORM_OOTB_MODULE_PREFIX_2019)
            )
            && File(file, HybrisConstants.EXTENSION_INFO_XML).isFile()
    }

    fun isMavenModule(rootProjectDirectory: File): Boolean {
        if (rootProjectDirectory.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false
        }
        return File(rootProjectDirectory, MavenConstants.POM_XML).isFile()
    }

    fun isEclipseModule(rootProjectDirectory: File): Boolean {
        if (rootProjectDirectory.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false
        }
        return File(rootProjectDirectory, HybrisConstants.DOT_PROJECT).isFile()
    }

    fun isGradleModule(file: File): Boolean {
        if (file.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false
        }
        return File(file, HybrisConstants.GRADLE_SETTINGS).isFile()
            || File(file, HybrisConstants.GRADLE_BUILD).isFile()
    }

    fun isGradleKtsModule(file: File): Boolean {
        if (file.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false
        }
        return File(file, HybrisConstants.GRADLE_SETTINGS_KTS).isFile()
            || File(file, HybrisConstants.GRADLE_BUILD_KTS).isFile()
    }

    fun hasVCS(rootProjectDirectory: File?) = File(rootProjectDirectory, ".git").isDirectory()
        || File(rootProjectDirectory, ".svn").isDirectory()
        || File(rootProjectDirectory, ".hg").isDirectory()

    companion object {
        fun getInstance(): HybrisProjectService = application.service()
    }
}