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

package sap.commerce.toolset.cockpitNG.psi.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import sap.commerce.toolset.cockpitNG.psi.CngPsiHelper
import sap.commerce.toolset.typeSystem.psi.reference.AbstractAttributeDeclarationReference
import sap.commerce.toolset.typeSystem.psi.reference.result.AttributeResolveResult
import sap.commerce.toolset.typeSystem.psi.reference.result.RelationEndResolveResult

class CngTSItemAttributeReference(
    element: PsiElement,
    textRange: TextRange,
    private val previousReference: CngTSItemAttributeReference? = null
) : AbstractAttributeDeclarationReference(element, textRange) {

    override fun resolveType(element: PsiElement): String? {
        if (previousReference == null || previousReference == this) return CngPsiHelper.resolveContextType(element)

        return previousReference.multiResolve(true)
            .firstOrNull()
            ?.let {
                when (it) {
                    is AttributeResolveResult -> it.meta.type
                    is RelationEndResolveResult -> it.meta.type
                    else -> null
                }
            }
    }
}
