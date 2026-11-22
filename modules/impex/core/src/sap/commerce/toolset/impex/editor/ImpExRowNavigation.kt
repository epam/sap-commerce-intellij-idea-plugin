/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
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

package sap.commerce.toolset.impex.editor

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import sap.commerce.toolset.impex.psi.ImpExValueLine

/**
 * Finds the same declarations as the navigation from the corresponding elements of the ImpEx file does.
 */
object ImpExRowNavigation {

    fun headerTypeDeclaration(valueLine: ImpExValueLine): PsiElement? = declarationOf(valueLine.headerLine?.fullHeaderType?.headerTypeName)

    fun subTypeDeclaration(valueLine: ImpExValueLine): PsiElement? = declarationOf(valueLine.subTypeName)

    fun columnDeclaration(valueLine: ImpExValueLine, columnIndex: Int): PsiElement? = declarationOf(
        valueLine.valueGroupList.getOrNull(columnIndex)?.fullHeaderParameter?.anyHeaderParameterName
    )

    private fun declarationOf(element: PsiElement?): PsiElement? = element
        ?.references
        ?.asSequence()
        ?.flatMap { reference ->
            if (reference is PsiPolyVariantReference) reference.multiResolve(false).asSequence().mapNotNull { it.element }
            else listOfNotNull(reference.resolve()).asSequence()
        }
        ?.firstOrNull()
}
