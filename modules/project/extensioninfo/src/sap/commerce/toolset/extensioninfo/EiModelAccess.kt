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

package sap.commerce.toolset.extensioninfo

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.extensioninfo.context.Dependency
import sap.commerce.toolset.extensioninfo.context.ExtensionInfoContext
import sap.commerce.toolset.extensioninfo.jaxb.ExtensionType
import sap.commerce.toolset.extensioninfo.model.ExtensionInfo
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path

@Service
class EiModelAccess {

    fun getExtensionInfo(module: Module) = ModuleRootManager.getInstance(module).contentRoots
        .firstNotNullOfOrNull { it.findFile(EiConstants.EXTENSION_INFO_XML) }
        ?.let { PsiManager.getInstance(module.project).findFile(it) }
        ?.asSafely<XmlFile>()
        ?.let { DomManager.getDomManager(module.project).getFileElement(it, ExtensionInfo::class.java) }
        ?.rootElement
        ?.extension

    fun getContext(moduleRootPath: Path): ExtensionInfoContext? = unmarshallExtensionInfo(moduleRootPath)
        ?.let { extension ->
            val metas = extension.meta
                .associate { it.key to it.value }

            ExtensionInfoContext(
                name = extension.name,
                description = extension.description,
                useMaven = "true".equals(extension.usemaven, ignoreCase = true),
                webModule = extension.webmodule != null
                    && moduleRootPath.resolve(EiConstants.Extension.WEB).directoryExists,
                hmcModule = extension.hmcmodule != null,
                coreModule = extension.coremodule != null,
                jaloLogicFree = extension.isJaloLogicFree,
                packageRoot = extension.coremodule?.packageroot,
                webRoot = extension.webmodule?.webroot,
                version = extension.version,
                requiredByAll = extension.isRequiredbyall,
                classPathGen = metas[EiConstants.EXTENSION_META_KEY_CLASSPATHGEN],
                moduleGenName = metas[EiConstants.EXTENSION_META_KEY_MODULE_GEN],
                deprecated = isMetaKeySetToTrue(metas, EiConstants.EXTENSION_META_KEY_DEPRECATED),
                hacModule = isMetaKeySetToTrue(metas, EiConstants.EXTENSION_META_KEY_HAC_MODULE),
                backofficeModule = isMetaKeySetToTrue(metas, EiConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE)
                    && moduleRootPath.resolve(EiConstants.Extension.BACK_OFFICE).directoryExists,
                extGenTemplateExtension = isMetaKeySetToTrue(metas, EiConstants.EXTENSION_META_KEY_EXT_GEN),
                requiredExtensions = extension.requiresExtension
                    .filter { it.name.isNotBlank() }
                    .map { Dependency(it.name, it.version) }
            )
        }

    private fun unmarshallExtensionInfo(moduleRootPath: Path): ExtensionType? = try {
        EiUnmarshaller.unmarshall(moduleRootPath).extension
            .takeUnless { it.name.isNullOrBlank() }
    } catch (e: HybrisConfigurationException) {
        thisLogger().warn(e)
        null
    }

    private fun isMetaKeySetToTrue(metas: Map<String, String>, metaKeyName: String) = metas[metaKeyName]
        ?.let { "true".equals(it, true) }
        ?: false

    companion object {
        fun getInstance(): EiModelAccess = application.service()
    }
}
