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

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.getOrCreateUserDataUnsafe
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.xmlb.annotations.OptionTag
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.extensioninfo.EiUnmarshaller
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.SubModuleDescriptorType
import java.io.File
import java.io.Serial

data class ExtensionDescriptor(
    @OptionTag val name: String = "",
    @OptionTag val path: String = "",
    @OptionTag val type: ModuleDescriptorType = ModuleDescriptorType.NONE,
    @OptionTag val subModuleType: SubModuleDescriptorType? = null,
    @OptionTag var readonly: Boolean = false,
    @OptionTag val addon: Boolean = false,
    @OptionTag val installedIntoExtensions: Set<String> = emptySet(),
) : UserDataHolderBase() {

    val info
        get() = getOrCreateUserDataUnsafe(KEY_INFO) {
            if (path.isEmpty() || subModuleType != null) return Info()

            try {
                val path = FileUtil.toSystemDependentName(path)
                val moduleRootDirectory = File(path)
                val extensionInfo = EiUnmarshaller.unmarshall(moduleRootDirectory)
                val metas = extensionInfo.extension.meta
                    .associate { it.key to it.value }

                Info(
                    description = extensionInfo.extension.description,
                    useMaven = "true".equals(extensionInfo.extension.usemaven, ignoreCase = true),
                    webModule = extensionInfo.extension.webmodule != null,
                    hmcModule = extensionInfo.extension.hmcmodule != null,
                    coreModule = extensionInfo.extension.coremodule != null,
                    jaloLogicFree = extensionInfo.extension.isJaloLogicFree,
                    packageRoot = extensionInfo.extension.coremodule?.packageroot,
                    webRoot = extensionInfo.extension.webmodule?.webroot,
                    version = extensionInfo.extension.version,
                    requiredByAll = extensionInfo.extension.isRequiredbyall,
                    classPathGen = metas[HybrisConstants.EXTENSION_META_KEY_CLASSPATHGEN],
                    moduleGenName = metas[HybrisConstants.EXTENSION_META_KEY_MODULE_GEN],
                    deprecated = isMetaKeySetToTrue(metas, HybrisConstants.EXTENSION_META_KEY_DEPRECATED),
                    hacModule = isMetaKeySetToTrue(metas, HybrisConstants.EXTENSION_META_KEY_HAC_MODULE),
                    backofficeModule = isMetaKeySetToTrue(metas, HybrisConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE),
                    extGenTemplateExtension = isMetaKeySetToTrue(metas, HybrisConstants.EXTENSION_META_KEY_EXT_GEN),
                )
            } catch (e: HybrisConfigurationException) {
                thisLogger().debug(e)
                Info()
            }
        }

    private fun isMetaKeySetToTrue(metas: Map<String, String>, metaKeyName: String) = metas[metaKeyName]
        ?.let { "true".equals(it, true) }
        ?: false

    data class Info(
        var description: String? = null,
        var classPathGen: String? = null,
        var moduleGenName: String? = null,
        var packageRoot: String? = null,
        var webRoot: String? = null,
        var version: String? = null,
        var backofficeModule: Boolean = false,
        var useMaven: Boolean = false,
        var hacModule: Boolean = false,
        var webModule: Boolean = false,
        var hmcModule: Boolean = false,
        var coreModule: Boolean = false,
        var deprecated: Boolean = false,
        var extGenTemplateExtension: Boolean = false,
        var jaloLogicFree: Boolean = false,
        var requiredByAll: Boolean = false,
    )

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4281602487713822529L
        private val KEY_INFO = Key<Info>("sap.commerce.toolset.extensioninfo")
    }
}
