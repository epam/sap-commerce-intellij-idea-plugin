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

package sap.commerce.toolset.impex.lang.documentation

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import sap.commerce.toolset.impex.psi.ImpExFile
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.settings.yDeveloperSettings

class ImpExDocumentationTargetProvider : DocumentationTargetProvider {

    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> {
        if (file !is ImpExFile) return emptyList()

        val element = file.findElementAt(offset) ?: return emptyList()

        if (!file.project.isHybrisProject) return emptyList()

        val developerSettings = file.project.yDeveloperSettings
        val documentationSettings = developerSettings.impexSettings.documentation
        if (!documentationSettings.enabled) return emptyList()

        val allowedElementTypes = with(mutableListOf<IElementType>()) {
            if (documentationSettings.showTypeDocumentation) {
                add(ImpExTypes.HEADER_TYPE)
                add(ImpExTypes.VALUE_SUBTYPE)
            }
            if (documentationSettings.showModifierDocumentation) {
                add(ImpExTypes.ATTRIBUTE_NAME)
                add(ImpExTypes.FUNCTION)
                add(ImpExTypes.HEADER_PARAMETER_NAME)
            }
            this
        }

        if (!allowedElementTypes.contains(element.elementType)) return emptyList()

        return when (element.elementType) {
            ImpExTypes.HEADER_TYPE,
            ImpExTypes.VALUE_SUBTYPE,
            ImpExTypes.ATTRIBUTE_NAME,
            ImpExTypes.FUNCTION,
            ImpExTypes.HEADER_PARAMETER_NAME -> arrayListOf(ImpExDocumentationTarget(element, element))

            else -> emptyList()
        }
    }
}