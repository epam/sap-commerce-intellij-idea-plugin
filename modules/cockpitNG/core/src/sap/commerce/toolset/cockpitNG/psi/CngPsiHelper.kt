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

package sap.commerce.toolset.cockpitNG.psi

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag
import sap.commerce.toolset.HybrisConstants

object CngPsiHelper {

    fun resolveContextTag(element: PsiElement) = element.parentsOfType<XmlTag>()
        .firstOrNull { it.localName == "context" && it.getAttribute("type") != null }

    fun resolveContextType(element: PsiElement) = resolveContextTag(element)
        ?.getAttributeValue("type")

    fun resolveContextTypeForNewItemInWizardFlow(element: PsiElement) = resolveNamedContextTypeForNewItemInWizardFlow(element)
        ?.second

    fun resolveNamedContextTypeForNewItemInWizardFlow(element: PsiElement): Pair<String?, String?>? {
        val newItemName = element.parentsOfType<XmlTag>()
            .firstOrNull { it.localName == "property-list" }
            ?.getAttributeValue("root")
            ?: resolveInlineItemName(element)
            ?: return null to resolveContextType(element)

        val initializeProperty = element.parentsOfType<XmlTag>()
            .firstOrNull { it.localName == "flow" }
            ?.childrenOfType<XmlTag>()
            ?.find { it.localName == "prepare" }
            ?.childrenOfType<XmlTag>()
            ?.find { it.localName == "initialize" && it.getAttributeValue("property") == newItemName }

        // ignore code completion and reference resolution for template-bean properties, corresponding Inspection will suggest conversion into plain type
        val templateBean = initializeProperty?.getAttributeValue("template-bean")
        if (templateBean != null) return newItemName to "SPRING_BEAN_$templateBean"

        val newItemType = initializeProperty?.getAttributeValue("type")
            ?: return newItemName to resolveContextType(element)

        return if (HybrisConstants.COCKPIT_NG_INITIALIZE_CONTEXT_TYPE.equals(newItemType, true)) newItemName to resolveContextType(element)
        else newItemName to newItemType
    }

    private fun resolveInlineItemName(element: PsiElement) = element
        .text
        .replace("\"", "")
        .takeIf { it.contains(".") }
        ?.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "")
        ?.split(".")
        ?.firstOrNull()
        ?.trim()

}