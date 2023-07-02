/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.flexibleSearch.lang.documentation

import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFile
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchDefinedTableName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType

class FlexibleSearchDocumentationTargetProvider : DocumentationTargetProvider {

    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> {
        if (file !is FlexibleSearchFile) return emptyList()

        val element = file.findElementAt(offset) ?: return emptyList()
        val settingsComponent = HybrisProjectSettingsComponent.getInstance(file.project)

        if (!settingsComponent.isHybrisProject()) return emptyList()

        val documentationSettings = settingsComponent.state.flexibleSearchSettings.documentation
        if (!documentationSettings.enabled) return emptyList()

        val allowedElementTypes = with(mutableListOf<IElementType>()) {
            if (documentationSettings.showTypeDocumentation && element.parent is FlexibleSearchDefinedTableName) {
                add(FlexibleSearchTypes.IDENTIFIER)
            }
            this
        }

        if (!allowedElementTypes.contains(element.elementType)) return emptyList()

        return when (element.elementType) {
            FlexibleSearchTypes.IDENTIFIER -> arrayListOf(FlexibleSearchDocumentationTarget(element, element))

            else -> emptyList()
        }
    }
}