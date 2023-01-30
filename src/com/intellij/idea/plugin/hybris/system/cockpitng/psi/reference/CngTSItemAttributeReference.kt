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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.references.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.impex.psi.references.result.RelationElementResolveResult
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPsiHelper
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult

open class CngTSItemAttributeReference(element: PsiElement) : TSReferenceBase<PsiElement>(element), PsiPolyVariantReference, HighlightedReference {

    override fun calculateDefaultRangeInElement(): TextRange =
        if (element.textLength == 0) super.calculateDefaultRangeInElement()
        else TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val type = resolveType(element) ?: return emptyArray()

        val meta = metaModelAccess.findMetaItemByName(type) ?: return emptyArray()

        return metaItemService.findAttributesByName(meta, value, true)
            ?.firstOrNull()
            ?.retrieveDom()
            ?.let { arrayOf(AttributeResolveResult(it)) }
            ?: metaItemService.findRelationEndsByQualifier(meta, value, true)
                ?.firstOrNull()
                ?.retrieveDom()
                ?.let { arrayOf(RelationElementResolveResult(it)) }
            ?: emptyArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        if (resolveResults.size != 1) return null

        return with (resolveResults[0]) {
            if (this.isValidResult) return@with this.element
            return@with null
        }
    }

    protected open fun resolveType(element: PsiElement) = CngPsiHelper.resolveContextType(element)

}
