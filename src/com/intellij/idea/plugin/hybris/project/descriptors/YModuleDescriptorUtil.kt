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
import com.intellij.openapi.application.ApplicationManager
import io.ktor.util.*
import org.apache.commons.collections4.CollectionUtils
import java.io.File
import java.util.*

object YModuleDescriptorUtil {

    fun isPreselected(descriptor: ModuleDescriptor) = when (descriptor) {
        is CCv2ModuleDescriptor,
        is PlatformModuleDescriptor,
        is YPlatformExtModuleDescriptor -> true

        is YSubModuleDescriptor -> isPreselected(descriptor)

        is ConfigModuleDescriptor -> descriptor.isPreselected
        is YRegularModuleDescriptor -> descriptor.isInLocalExtensions
        else -> false
    }

    private fun isPreselected(descriptor: YSubModuleDescriptor): Boolean {
        return isPreselected(descriptor.owner)
    }

    fun hasKotlinDirectories(descriptor: ModuleDescriptor) = File(descriptor.moduleRootDirectory, HybrisConstants.KOTLIN_SRC_DIRECTORY).exists()
        || File(descriptor.moduleRootDirectory, HybrisConstants.KOTLIN_TEST_SRC_DIRECTORY).exists()

    fun getIdeaModuleFile(descriptor: ModuleDescriptor): File {
        val futureModuleName = descriptor.ideaModuleName()
        return descriptor.rootProjectDescriptor.modulesFilesDirectory
            ?.let { File(descriptor.rootProjectDescriptor.modulesFilesDirectory, futureModuleName + HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION) }
            ?: File(descriptor.moduleRootDirectory, futureModuleName + HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION)
    }

    fun getRelativePath(descriptor: ModuleDescriptor): String {
        val moduleRootDir: File = descriptor.moduleRootDirectory
        val projectRootDir: File = descriptor.rootProjectDescriptor.rootDirectory ?: return moduleRootDir.path
        val virtualFileSystemService = ApplicationManager.getApplication().getService(VirtualFileSystemService::class.java)

        return if (virtualFileSystemService.fileContainsAnother(projectRootDir, moduleRootDir)) {
            virtualFileSystemService.getRelativePath(projectRootDir, moduleRootDir)
        } else moduleRootDir.path
    }
    // TODO: evaluate it only once during import, see usages
    fun getRequiredExtensionNames(descriptor: YModuleDescriptor) = when (descriptor) {
        is YRegularModuleDescriptor -> getRequiredExtensionNames(descriptor)
        // TODO: build web dependencies
//        is YWebSubModuleDescriptor -> getRequiredExtensionNames(descriptor)
        is YAcceleratorAddonSubModuleDescriptor -> getRequiredExtensionNames(descriptor)
        // TODO: build BO dependencies
//        is YBackofficeSubModuleDescriptor -> getRequiredExtensionNames(descriptor)
        is YHacSubModuleDescriptor -> getRequiredExtensionNames(descriptor)
        is YSubModuleDescriptor -> getRequiredExtensionNames(descriptor)
        else -> emptySet()
    }

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

        if (descriptor.hasHmcModule) {
            requiredExtensionNames.add(HybrisConstants.EXTENSION_NAME_HMC)
        }
        // TODO: mby move it to BackofficeSubModule
        if (descriptor.hasBackofficeModule) {
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
        return setOf(descriptor.owner.name) + webNames
    }

    private fun getRequiredExtensionNames(descriptor: YHacSubModuleDescriptor) = setOf(
        descriptor.owner.name,
        HybrisConstants.EXTENSION_NAME_HAC + ".web"
    )

    private fun getRequiredExtensionNames(descriptor: YSubModuleDescriptor): Set<String> {
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
//    fun hasHmcModule(descriptor: YRegularModuleDescriptor) = descriptor.extensionInfo.extension
//        .hmcmodule != null
//
//    fun isHacAddon(descriptor: YRegularModuleDescriptor) = isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_HAC_MODULE)
//
//    // TODO: validate usage
//    fun hasBackofficeModule(descriptor: YRegularModuleDescriptor) = isMetaKeySetToTrue(descriptor, HybrisConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE)
//        && File(descriptor.rootDirectory, HybrisConstants.BACKOFFICE_MODULE_DIRECTORY).isDirectory

//    fun hasWebModule(descriptor: YRegularModuleDescriptor) = descriptor.extensionInfo.extension.webmodule != null
//        && File(descriptor.rootDirectory, HybrisConstants.WEB_MODULE_DIRECTORY).isDirectory

//    fun isMetaKeySetToTrue(descriptor: YRegularModuleDescriptor, metaKeyName: String) = descriptor.metas[metaKeyName]
//        ?.let { "true".equals(it, true) }
//        ?: false

}