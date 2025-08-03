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
package com.intellij.idea.plugin.hybris.project

import com.intellij.ide.IconProvider
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import javax.swing.Icon

class HybrisProjectIconProvider : IconProvider() {

    override fun getIcon(element: PsiElement, p1: Int) = when (element) {
        is PsiDirectory -> getIcon(element)
        is PsiFile -> getIcon(element)
        else -> null
    }

    private fun getIcon(file: PsiFile): Icon? = when {
        file.name == HybrisConstants.BUILD_CALLBACKS_XML -> HybrisIcons.BuildCallbacks.FILE
        file.name == HybrisConstants.UNMANAGED_DEPENDENCIES_TXT -> HybrisIcons.UnmanagedDependencies.FILE
        file.name == HybrisConstants.EXTERNAL_DEPENDENCIES_XML -> HybrisIcons.ExternalDependencies.FILE
        file.name == HybrisConstants.HYBRIS_LICENCE_JAR
            || file.name == HybrisConstants.SAP_LICENCES -> HybrisIcons.Y.LICENCE

        file.name == HybrisConstants.EXTENSION_INFO_XML -> HybrisIcons.ExtensionInfo.FILE
        file.name == HybrisConstants.LOCAL_EXTENSIONS_XML -> HybrisIcons.LocalExtensions.FILE
        file.name.endsWith(HybrisConstants.IMPORT_OVERRIDE_FILENAME) -> HybrisIcons.PLUGIN_SETTINGS
        else -> null
    }

    private fun getIcon(directory: PsiDirectory): Icon? {
        val parentName = directory.parentDirectory?.name
        return when (directory.name) {
            "tomcat" if (parentName == HybrisConstants.EXTENSION_NAME_CONFIG || parentName == HybrisConstants.EXTENSION_NAME_PLATFORM) -> HybrisIcons.Tools.TOMCAT
            "solr" if parentName == HybrisConstants.EXTENSION_NAME_CONFIG -> HybrisIcons.Tools.SOLR
            else -> null
        }
    }
}
