/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.psi.util

import com.intellij.psi.PsiElement
import sap.commerce.toolset.impex.psi.*

fun setName(element: PsiElement, newName: String): PsiElement {
    if (element is ImpExValue) {
        val newElement = ImpExElementFactory.createValueElement(element.project, newName) ?: return element
        element.node.treeParent?.replaceChild(element.node, newElement.node) ?: return element
        return newElement
    }

    val keyNode = element.node.firstChildNode
    if (keyNode != null) {
        val property = when (element) {
            is ImpExMacroNameDec -> ImpExElementFactory.createMacrosDecElement(element.project, newName)
            is ImpExMacroUsageDec -> ImpExElementFactory.createMacrosUsageElement(element.project, newName)
            is ImpExDocumentIdDec -> ImpExElementFactory.createDocumentIdDecElement(element.project, newName)
            is ImpExDocumentIdUsage -> ImpExElementFactory.createDocumentIdUsageElement(element.project, newName)
            else -> null
        }
        val newKeyNode = if (property == null) return element else property.node
        element.node.replaceChild(keyNode, newKeyNode!!)
    }
    return element
}
