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

package com.intellij.idea.plugin.hybris.impex.lang.documentation

import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType

class ImpexDocumentationTargetProvider : DocumentationTargetProvider {

    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> {
        if (file !is ImpexFile) return emptyList()

        val element = file.findElementAt(offset) ?: return emptyList()
        val projectSettings = HybrisProjectSettingsComponent.getInstance(file.project)

        if (!projectSettings.isHybrisProject()) return emptyList()

        val developerSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(file.project).state
        val documentationSettings = developerSettings.impexSettings.documentation
        if (!documentationSettings.enabled) return emptyList()

        val allowedElementTypes = with(mutableListOf<IElementType>()) {
            if (documentationSettings.showTypeDocumentation) {
                add(ImpexTypes.HEADER_TYPE)
                add(ImpexTypes.VALUE_SUBTYPE)
            }
            if (documentationSettings.showModifierDocumentation) {
                add(ImpexTypes.ATTRIBUTE_NAME)
                add(ImpexTypes.FUNCTION)
                add(ImpexTypes.HEADER_PARAMETER_NAME)
            }
            this
        }

        if (!allowedElementTypes.contains(element.elementType)) return emptyList()

        return when (element.elementType) {
            ImpexTypes.HEADER_TYPE,
            ImpexTypes.VALUE_SUBTYPE,
            ImpexTypes.ATTRIBUTE_NAME,
            ImpexTypes.FUNCTION,
            ImpexTypes.HEADER_PARAMETER_NAME -> arrayListOf(ImpexDocumentationTarget(element, element))

            else -> emptyList()
        }
    }
}