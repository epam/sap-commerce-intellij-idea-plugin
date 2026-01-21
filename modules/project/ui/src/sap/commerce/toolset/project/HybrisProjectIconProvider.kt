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

import com.intellij.icons.AllIcons
import com.intellij.ide.IconProvider
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlFile
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.externalDependencies.EdConstants
import sap.commerce.toolset.localextensions.LeConstants
import javax.swing.Icon

class HybrisProjectIconProvider : IconProvider() {

    override fun getIcon(element: PsiElement, p1: Int) = when (element) {
        is PsiDirectory -> getIcon(element)
        is PsiFile -> getIcon(element)
        else -> null
    }

    private fun getIcon(file: PsiFile): Icon? = when {
        file.name == ProjectConstants.File.BUILD_CALLBACKS_XML -> HybrisIcons.BuildCallbacks.FILE
        file.name == EdConstants.UNMANAGED_DEPENDENCIES_TXT -> HybrisIcons.UnmanagedDependencies.FILE
        file.name == EdConstants.EXTERNAL_DEPENDENCIES_XML -> HybrisIcons.ExternalDependencies.FILE
        file.name == ProjectConstants.File.HYBRIS_LICENCE_JAR
            || file.name == ProjectConstants.File.SAP_LICENCES -> HybrisIcons.Y.LICENCE

        file.name == EiConstants.EXTENSION_INFO_XML -> HybrisIcons.ExtensionInfo.FILE
        file.name == LeConstants.LOCAL_EXTENSIONS_XML -> HybrisIcons.LocalExtensions.FILE
        file.name.endsWith(HybrisConstants.IMPORT_OVERRIDE_FILENAME) -> HybrisIcons.PLUGIN_SETTINGS
        else -> null
    }

    private fun getIcon(directory: PsiDirectory): Icon? {
        val parentName = directory.parentDirectory?.name
        val directoryName = directory.name
        return when {
            directoryName == "tomcat" && (parentName == EiConstants.Extension.CONFIG || parentName == EiConstants.Extension.PLATFORM) -> HybrisIcons.Tools.TOMCAT
            directoryName == "solr" && parentName == EiConstants.Extension.CONFIG -> HybrisIcons.Tools.SOLR
            parentName == ProjectConstants.Directory.RESOURCES && (directoryName == "localization" || directoryName.endsWith("-backoffice-labels")) -> AllIcons.FileTypes.I18n
            directoryName == "lib" && (directory.parentDirectory?.childrenOfType<XmlFile>()?.any { it.name == EiConstants.EXTENSION_INFO_XML } ?: false) -> HybrisIcons.Module.LIB
            else -> null
        }
    }
}