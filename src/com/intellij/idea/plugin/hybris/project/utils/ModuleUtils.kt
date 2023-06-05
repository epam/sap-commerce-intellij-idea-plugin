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

package com.intellij.idea.plugin.hybris.project.utils

import com.intellij.idea.plugin.hybris.facet.YFacet
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement

object ModuleUtils {

    fun isHybrisModule(psi: PsiElement): Boolean {
        val module = ModuleUtilCore.findModuleForPsiElement(psi) ?: return false
        val descriptorType = HybrisProjectSettingsComponent.getInstance(psi.project)
            .getModuleSettings(module).descriptorType
        return descriptorType == ModuleDescriptorType.PLATFORM
            || descriptorType == ModuleDescriptorType.EXT
    }

    fun getShortName(module: Module) = YFacet.get(module)
        ?.configuration
        ?.state
        ?.name
        ?: module.name.substringAfterLast(".")
    fun getSubmoduleShortName(module: Module) = module.name.split(".")
        .takeLast(2)
        .joinToString(".")

}