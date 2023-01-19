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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag

object CngPsiHelper {

    fun resolveContextType(element: PsiElement) = element.parentsOfType<XmlTag>()
        .firstOrNull { it.localName == "context" && it.getAttribute("type") != null }
        ?.getAttributeValue("type")

    fun resolveContextTypeForNewItemInWizardFlow(element: PsiElement): String? {
        val newItemName = element.parentsOfType<XmlTag>()
            .firstOrNull { it.localName == "property-list" }
            ?.getAttributeValue("root") ?: return null
        val newItemType = element.parentsOfType<XmlTag>()
            .firstOrNull { it.localName == "flow" }
            ?.childrenOfType<XmlTag>()
            ?.find { it.localName == "prepare" }
            ?.childrenOfType<XmlTag>()
            ?.find { it.localName == "initialize" && it.getAttributeValue("property") == newItemName }
            ?.getAttributeValue("type")
            ?: return null

        return if ("ctx.TYPE_CODE".equals(newItemType, true)) resolveContextType(element)
        else newItemType
    }
}