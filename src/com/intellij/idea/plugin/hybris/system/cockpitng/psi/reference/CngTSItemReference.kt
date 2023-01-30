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
import com.intellij.idea.plugin.hybris.impex.psi.references.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.impex.psi.references.result.ItemResolveResult
import com.intellij.idea.plugin.hybris.impex.psi.references.result.RelationResolveResult
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaRelation
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult

/**
 * https://help.sap.com/docs/SAP_COMMERCE/5c9ea0c629214e42b727bf08800d8dfa/8b59d395866910149db8889c5087a5ef.html?locale=en-US&q=ExtendedMultiReference
 *
 * see, standard-editors-spring.xml
 */
open class CngTSItemReference(element: PsiElement) : TSReferenceBase<PsiElement>(element), PsiPolyVariantReference, HighlightedReference {

    override fun calculateDefaultRangeInElement(): TextRange {
        val text = element.text.trim()
            .replace("\"", "")

        return regexes
            .firstOrNull { it.matches(text) }
            ?.let { _ ->
                val offset = element.text.indexOfLast { it == '(' } + 1
                val length = element.text.indexOfFirst { it == ')' } - offset
                TextRange.from(offset, length)
            }
            ?: if (element.textLength == 0) super.calculateDefaultRangeInElement()
            else TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val lookingForName = value

        return metaModelAccess.findMetaItemByName(lookingForName)
            ?.let { resolve(it) }
            ?: metaModelAccess.findMetaEnumByName(lookingForName)
                ?.let { resolve(it) }
            ?: metaModelAccess.findMetaRelationByName(lookingForName)
                ?.let { resolve(it) }
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

    private fun resolve(meta: TSGlobalMetaItem): Array<ResolveResult> = meta.retrieveAllDoms()
        .map { ItemResolveResult(it) }
        .toTypedArray()

    private fun resolve(meta: TSGlobalMetaEnum): Array<ResolveResult> = meta.retrieveAllDoms()
        .map { EnumResolveResult(it) }
        .toTypedArray()

    private fun resolve(meta: TSGlobalMetaRelation): Array<ResolveResult> = meta.retrieveAllDoms()
        .map { RelationResolveResult(it) }
        .toTypedArray()

    companion object {
        private val regexes = arrayOf(
            "^Localized\\((.*)\\)\$".toRegex(),
            "^LocalizedSimple\\((.*)\\)\$".toRegex(),
            "^List\\((.*)\\)\$".toRegex(),
            "^Reference\\((.*)\\)\$".toRegex(),
            "^FixedValuesReference\\((.*)\\)\$".toRegex(),
            "^MultiReference-(COLLECTION|LIST|SET)\\((.*)\\)\$".toRegex(),
            "^ExtendedMultiReference-(COLLECTION|LIST|SET)\\((.*)\\)\$".toRegex(),
            "^EnumMultiReference-(COLLECTION|LIST|SET)\\((.*)\\)\$".toRegex(),
        )
    }

}
