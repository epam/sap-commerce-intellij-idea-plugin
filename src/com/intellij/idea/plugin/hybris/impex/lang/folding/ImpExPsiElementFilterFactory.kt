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
package com.intellij.idea.plugin.hybris.impex.lang.folding

import com.intellij.idea.plugin.hybris.impex.lang.folding.util.ImpExSimpleFoldingBlocksFilter
import com.intellij.idea.plugin.hybris.impex.lang.folding.util.ImpExSmartFoldingBlocksFilter
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiElementFilter
import com.intellij.util.application

class ImpExPsiElementFilterFactory private constructor() {
    init {
        throw IllegalAccessException("Should never be accessed.")
    }

    companion object {
        fun getPsiElementFilter(project: Project): PsiElementFilter = if (isUseSmartFolding(project))
            application.getService(ImpExSmartFoldingBlocksFilter::class.java)
        else
            application.getService(ImpExSimpleFoldingBlocksFilter::class.java)

        private fun isUseSmartFolding(project: Project) = DeveloperSettingsComponent.Companion.getInstance(project).getState()
            .impexSettings
            .folding
            .useSmartFolding

    }
}
