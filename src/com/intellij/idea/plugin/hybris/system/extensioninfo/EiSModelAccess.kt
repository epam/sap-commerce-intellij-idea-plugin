/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.extensioninfo

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.extensioninfo.model.ExtensionInfo
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.util.asSafely
import com.intellij.util.xml.DomManager

object EiSModelAccess {

    fun getExtensionInfo(psi: PsiElement) = ModuleUtil.findModuleForPsiElement(psi)
        ?.let { getExtensionInfo(it) }

    fun getExtensionInfo(module: Module) = ModuleRootManager.getInstance(module).contentRoots
        .firstNotNullOfOrNull { it.findFile(HybrisConstants.EXTENSION_INFO_XML) }
        ?.let { PsiManager.getInstance(module.project).findFile(it) }
        ?.asSafely<XmlFile>()
        ?.let { DomManager.getDomManager(module.project).getFileElement(it, ExtensionInfo::class.java) }
        ?.rootElement
        ?.extension

}