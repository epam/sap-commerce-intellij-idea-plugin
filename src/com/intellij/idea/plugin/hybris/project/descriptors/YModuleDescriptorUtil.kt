/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.project.descriptors

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.services.VirtualFileSystemService
import com.intellij.idea.plugin.hybris.settings.ExtensionDescriptor
import com.intellij.openapi.application.ApplicationManager
import io.ktor.util.*
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.io.filefilter.DirectoryFileFilter
import java.io.File
import java.io.FileFilter
import java.util.*

object YModuleDescriptorUtil {

    // TODO: migrate it to new [y] Facet
    fun getExtensionDescriptor(descriptor: HybrisModuleDescriptor) = when (descriptor) {
        is RegularHybrisModuleDescriptor -> ExtensionDescriptor(
            descriptor.getName(),
            getDescriptorType(descriptor),
            isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE),
            isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_HAC_MODULE),
            isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_DEPRECATED),
            isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_EXT_GEN),
            getRequiredExtensionNames(descriptor).contains(HybrisConstants.EXTENSION_NAME_ADDONSUPPORT),
            descriptor.metas.get(HybrisConstants.EXTENSION_META_KEY_CLASSPATHGEN),
            descriptor.metas.get(HybrisConstants.EXTENSION_META_KEY_MODULE_GEN)
        );
        else -> ExtensionDescriptor(
            descriptor.name,
            getDescriptorType(descriptor),
            backofficeModule = false,
            hacModule = false,
            deprecated = false,
            extGenTemplateExtension = false,
            addon = false,
            classPathGen = null,
            moduleGenName = null
        )
    }

    fun isPreselected(descriptor: HybrisModuleDescriptor) = when (descriptor) {
        is CCv2HybrisModuleDescriptor,
        is PlatformHybrisModuleDescriptor -> true

        is ConfigHybrisModuleDescriptor -> descriptor.isPreselected
        is RegularHybrisModuleDescriptor -> descriptor.isInLocalExtensions
        else -> false
    }

    fun getDescriptorType(descriptor: HybrisModuleDescriptor) = when (descriptor) {
        is CCv2HybrisModuleDescriptor -> HybrisModuleDescriptorType.CCV2
        is CustomHybrisModuleDescriptor -> HybrisModuleDescriptorType.CUSTOM
        is EclipseModuleDescriptor -> HybrisModuleDescriptorType.ECLIPSE
        is ExtHybrisModuleDescriptor -> HybrisModuleDescriptorType.EXT
        is GradleModuleDescriptor -> HybrisModuleDescriptorType.GRADLE
        is OotbHybrisModuleDescriptor -> HybrisModuleDescriptorType.OOTB
        is MavenModuleDescriptor -> HybrisModuleDescriptorType.MAVEN
        is PlatformHybrisModuleDescriptor -> HybrisModuleDescriptorType.PLATFORM
        is ConfigHybrisModuleDescriptor -> if (descriptor.isMainConfig) HybrisModuleDescriptorType.CONFIG
        else HybrisModuleDescriptorType.CUSTOM

        is RootModuleDescriptor -> HybrisModuleDescriptorType.NONE
        else -> HybrisModuleDescriptorType.NONE
    }

    fun hasKotlinDirectories(descriptor: HybrisModuleDescriptor) = File(descriptor.rootDirectory, HybrisConstants.KOTLIN_SRC_DIRECTORY).exists()
        || File(descriptor.rootDirectory, HybrisConstants.KOTLIN_TEST_SRC_DIRECTORY).exists()

    fun isAcceleratorAddOnModuleRoot(descriptor: HybrisModuleDescriptor) = File(descriptor.rootDirectory, HybrisConstants.ACCELERATOR_ADDON_DIRECTORY)
        .isDirectory

    fun getWebRoot(descriptor: HybrisModuleDescriptor): File? = when (descriptor) {
        is RegularHybrisModuleDescriptor -> File(descriptor.rootDirectory, HybrisConstants.WEB_ROOT_DIRECTORY_RELATIVE_PATH)
            .takeIf { it.exists() }

        else -> null
    }

    fun getIdeaModuleFile(descriptor: HybrisModuleDescriptor) = descriptor.rootProjectDescriptor.modulesFilesDirectory
        ?.let { File(descriptor.rootProjectDescriptor.modulesFilesDirectory, descriptor.name + HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION) }
        ?: File(descriptor.rootDirectory, descriptor.name + HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION)

    fun getRelativePath(descriptor: HybrisModuleDescriptor): String {
        val moduleRootDir: File = descriptor.rootDirectory
        val projectRootDir: File = descriptor.rootProjectDescriptor.rootDirectory ?: return moduleRootDir.path
        val virtualFileSystemService = ApplicationManager.getApplication().getService(VirtualFileSystemService::class.java)

        return if (virtualFileSystemService.fileContainsAnother(projectRootDir, moduleRootDir)) {
            virtualFileSystemService.getRelativePath(projectRootDir, moduleRootDir)
        } else moduleRootDir.path
    }

    fun getRequiredExtensionNames(descriptor: HybrisModuleDescriptor) = when (descriptor) {
        is PlatformHybrisModuleDescriptor -> getRequiredExtensionNames(descriptor)
        is RegularHybrisModuleDescriptor -> getRequiredExtensionNames(descriptor)
        else -> emptySet()
    }

    private fun getRequiredExtensionNames(descriptor: PlatformHybrisModuleDescriptor) = File(descriptor.rootDirectory, HybrisConstants.PLATFORM_EXTENSIONS_DIRECTORY_NAME)
        .takeIf { it.isDirectory }
        ?.listFiles(DirectoryFileFilter.DIRECTORY as FileFilter)
        ?.map { it.name }
        ?.toSet()
        ?: emptySet()

    private fun getRequiredExtensionNames(descriptor: RegularHybrisModuleDescriptor): Set<String> {
        val extension = descriptor.extensionInfo.extension ?: return getDefaultRequiredExtensionNames(descriptor)
        val requiresExtension = extension.requiresExtension
            .takeIf { it.isNotEmpty() } ?: return getDefaultRequiredExtensionNames(descriptor)

        val requiredExtensionNames = requiresExtension
            .filter { it.name.isNotBlank() }
            .map { it.name }
            .toMutableSet()

        requiredExtensionNames.addAll(getAdditionalRequiredExtensionNames(descriptor))

        if (hasHmcModule(descriptor)) {
            requiredExtensionNames.add(HybrisConstants.EXTENSION_NAME_HMC)
        }
        if (hasBackofficeModule(descriptor)) {
            requiredExtensionNames.add(HybrisConstants.EXTENSION_NAME_BACK_OFFICE)
        }
        return requiredExtensionNames.unmodifiable()
    }

    private fun getDefaultRequiredExtensionNames(descriptor: RegularHybrisModuleDescriptor) = when (descriptor) {
        is CoreHybrisModuleDescriptor -> emptySet()
        is ExtHybrisModuleDescriptor -> setOf(HybrisConstants.EXTENSION_NAME_CORE)
        else -> setOf(HybrisConstants.EXTENSION_NAME_PLATFORM)
    }

    private fun getAdditionalRequiredExtensionNames(descriptor: RegularHybrisModuleDescriptor) = when (descriptor) {
        is CustomHybrisModuleDescriptor,
        is ExtHybrisModuleDescriptor -> emptySet()

        else -> setOf(HybrisConstants.EXTENSION_NAME_PLATFORM)
    }


    fun getDependenciesPlainList(moduleDescriptor: HybrisModuleDescriptor) = recursivelyCollectDependenciesPlainSet(moduleDescriptor, TreeSet())
        .unmodifiable()

    private fun recursivelyCollectDependenciesPlainSet(descriptor: HybrisModuleDescriptor, dependenciesSet: MutableSet<HybrisModuleDescriptor>): Set<HybrisModuleDescriptor> {
        val dependenciesTree = descriptor.dependenciesTree

        if (CollectionUtils.isEmpty(dependenciesTree)) return dependenciesSet

        for (moduleDescriptor in dependenciesTree) {
            if (dependenciesSet.contains(moduleDescriptor)) {
                continue
            }
            dependenciesSet.add(moduleDescriptor)
            dependenciesSet.addAll(recursivelyCollectDependenciesPlainSet(moduleDescriptor, dependenciesSet))
        }
        return dependenciesSet
    }

    fun hasHmcModule(descriptor: RegularHybrisModuleDescriptor) = descriptor
        .extensionInfo.extension.hmcmodule != null

    fun isHacAddon(descriptor: RegularHybrisModuleDescriptor) = isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_HAC_MODULE)

    fun hasBackofficeModule(descriptor: RegularHybrisModuleDescriptor) = isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE)
        && doesBackofficeDirectoryExist(descriptor)

    private fun isMetaKeySetToTrue(descriptor: RegularHybrisModuleDescriptor, metaKeyName: String) = descriptor.metas[metaKeyName]
        ?.let { it == java.lang.Boolean.TRUE.toString() }
        ?: false

    private fun doesBackofficeDirectoryExist(descriptor: RegularHybrisModuleDescriptor) = File(descriptor.rootDirectory, HybrisConstants.BACKOFFICE_MODULE_DIRECTORY)
        .isDirectory
}