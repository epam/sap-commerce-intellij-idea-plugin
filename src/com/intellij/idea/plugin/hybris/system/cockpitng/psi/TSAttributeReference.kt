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

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.references.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.impex.psi.references.result.RelationElementResolveResult
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Context
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.findParentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager

class TSAttributeReference(element: PsiElement) : TSReferenceBase<PsiElement>(element), PsiPolyVariantReference, HighlightedReference {

    override fun calculateDefaultRangeInElement() = TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val type = resolveContext()
            ?.type
            ?.stringValue
            ?: return emptyArray()

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

    private fun resolveContext(): Context? {
        var parent = element.findParentOfType<XmlTag>()

        while (parent != null && (parent.name != "context" || parent.getAttributeValue("type") == null)) {
            parent = parent.findParentOfType()
        }
        return DomManager.getDomManager(element.project).getDomElement(parent) as? Context
    }

}
