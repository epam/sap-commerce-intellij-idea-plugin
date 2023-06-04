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
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*
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
    fun getExtensionDescriptor(descriptor: ModuleDescriptor) = when (descriptor) {
        is YRegularModuleDescriptor -> ExtensionDescriptor(
            descriptor.name,
            descriptor.descriptorType,
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
            descriptor.descriptorType,
            backofficeModule = false,
            hacModule = false,
            deprecated = false,
            extGenTemplateExtension = false,
            addon = false,
            classPathGen = null,
            moduleGenName = null
        )
    }

    fun isPreselected(descriptor: ModuleDescriptor) = when (descriptor) {
        is CCv2ModuleDescriptor,
        is YPlatformModuleDescriptor,
        is YPlatformExtModuleDescriptor -> true

        is YSubModuleDescriptor -> isPreselected(descriptor)

        is YConfigModuleDescriptor -> descriptor.isPreselected
        is YRegularModuleDescriptor -> descriptor.isInLocalExtensions
        else -> false
    }

    private fun isPreselected(descriptor: YSubModuleDescriptor): Boolean {
        return isPreselected(descriptor.owner)
    }

    fun hasKotlinDirectories(descriptor: ModuleDescriptor) = File(descriptor.rootDirectory, HybrisConstants.KOTLIN_SRC_DIRECTORY).exists()
        || File(descriptor.rootDirectory, HybrisConstants.KOTLIN_TEST_SRC_DIRECTORY).exists()

    fun getIdeaModuleFile(descriptor: ModuleDescriptor): File {
        val futureModuleName = descriptor.groupNames.joinToString(separator = ".", postfix = ".") + descriptor.name
        return descriptor.rootProjectDescriptor.modulesFilesDirectory
            ?.let { File(descriptor.rootProjectDescriptor.modulesFilesDirectory, futureModuleName + HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION) }
            ?: File(descriptor.rootDirectory, futureModuleName + HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION)
    }

    fun getRelativePath(descriptor: ModuleDescriptor): String {
        val moduleRootDir: File = descriptor.rootDirectory
        val projectRootDir: File = descriptor.rootProjectDescriptor.rootDirectory ?: return moduleRootDir.path
        val virtualFileSystemService = ApplicationManager.getApplication().getService(VirtualFileSystemService::class.java)

        return if (virtualFileSystemService.fileContainsAnother(projectRootDir, moduleRootDir)) {
            virtualFileSystemService.getRelativePath(projectRootDir, moduleRootDir)
        } else moduleRootDir.path
    }

    fun getRequiredExtensionNames(descriptor: ModuleDescriptor) = when (descriptor) {
        is YPlatformModuleDescriptor -> getRequiredExtensionNames(descriptor)
        is YRegularModuleDescriptor -> getRequiredExtensionNames(descriptor)
//        is YWebSubModuleDescriptor -> getRequiredExtensionNames(descriptor)
        is YAcceleratorAddonSubModuleDescriptor -> getRequiredExtensionNames(descriptor)
//        is YBackofficeSubModuleDescriptor -> getRequiredExtensionNames(descriptor)
        is YSubModuleDescriptor -> getRequiredExtensionNames(descriptor)
        else -> emptySet()
    }

    private fun getRequiredExtensionNames(descriptor: YPlatformModuleDescriptor) = File(descriptor.rootDirectory, HybrisConstants.PLATFORM_EXTENSIONS_DIRECTORY_NAME)
        .takeIf { it.isDirectory }
        ?.listFiles(DirectoryFileFilter.DIRECTORY as FileFilter)
        ?.map { it.name }
        ?.toSet()
        ?: emptySet()

    private fun getRequiredExtensionNames(descriptor: YRegularModuleDescriptor): Set<String> {
        val extension = descriptor.extensionInfo.extension ?: return getDefaultRequiredExtensionNames(descriptor)
        val requiresExtension = extension.requiresExtension
            .takeIf { it.isNotEmpty() }
            ?: return getDefaultRequiredExtensionNames(descriptor)

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

    private fun getRequiredExtensionNames(descriptor: YWebSubModuleDescriptor): Set<String> {
        val ownerRequiredExtensionNames = getRequiredExtensionNames(descriptor.owner)

        val webNames = ownerRequiredExtensionNames
            .map { it + "." + HybrisConstants.WEB_MODULE_DIRECTORY }
        return setOf(descriptor.owner.name) + webNames
    }

    private fun getRequiredExtensionNames(descriptor: YBackofficeSubModuleDescriptor): Set<String> {
        val ownerRequiredExtensionNames = getRequiredExtensionNames(descriptor.owner)

        val backofficeNames = ownerRequiredExtensionNames
            .map { it + "." + HybrisConstants.BACKOFFICE_MODULE_DIRECTORY }
        return ownerRequiredExtensionNames + backofficeNames
    }

    private fun getRequiredExtensionNames(descriptor: YAcceleratorAddonSubModuleDescriptor): Set<String> {
        val ownerRequiredExtensionNames = getRequiredExtensionNames(descriptor.owner)

        val webNames = ownerRequiredExtensionNames
            .map { it + "." + HybrisConstants.WEB_MODULE_DIRECTORY }
//        val addonsNames = ownerRequiredExtensionNames
//            .map { it + "." + HybrisConstants.ACCELERATOR_ADDON_DIRECTORY }
        return setOf(descriptor.owner.name) + webNames
    }

    private fun getRequiredExtensionNames(descriptor: YSubModuleDescriptor): Set<String> {
//        return getRequiredExtensionNames(descriptor.owner)
        return setOf(descriptor.owner.name)
    }

    private fun getDefaultRequiredExtensionNames(descriptor: YRegularModuleDescriptor) = when (descriptor) {
        is YCoreExtModuleDescriptor -> emptySet()
        is YPlatformExtModuleDescriptor -> setOf(HybrisConstants.EXTENSION_NAME_CORE)
        else -> setOf(HybrisConstants.EXTENSION_NAME_PLATFORM)
    }

    private fun getAdditionalRequiredExtensionNames(descriptor: YRegularModuleDescriptor) = when (descriptor) {
        is YCustomRegularModuleDescriptor,
        is YPlatformExtModuleDescriptor -> emptySet()

        else -> setOf(HybrisConstants.EXTENSION_NAME_PLATFORM)
    }

    fun getDependenciesPlainList(moduleDescriptor: YModuleDescriptor) = recursivelyCollectDependenciesPlainSet(moduleDescriptor, TreeSet())
        .unmodifiable()

    private fun recursivelyCollectDependenciesPlainSet(descriptor: YModuleDescriptor, dependenciesSet: MutableSet<YModuleDescriptor>): Set<YModuleDescriptor> {
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

    // TODO: validate usage
    fun hasHmcModule(descriptor: YRegularModuleDescriptor) = descriptor.extensionInfo.extension
        .hmcmodule != null

    fun isHacAddon(descriptor: YRegularModuleDescriptor) = isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_HAC_MODULE)

    // TODO: validate usage
    fun hasBackofficeModule(descriptor: YRegularModuleDescriptor) = isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE)
        && File(descriptor.rootDirectory, HybrisConstants.BACKOFFICE_MODULE_DIRECTORY).isDirectory

    fun hasWebModule(descriptor: YRegularModuleDescriptor) = descriptor.extensionInfo.extension.webmodule != null
        && File(descriptor.rootDirectory, HybrisConstants.WEB_MODULE_DIRECTORY).isDirectory

    private fun isMetaKeySetToTrue(descriptor: YRegularModuleDescriptor, metaKeyName: String) = descriptor.metas[metaKeyName]
        ?.let { "true".equals(it, true) }
        ?: false

}