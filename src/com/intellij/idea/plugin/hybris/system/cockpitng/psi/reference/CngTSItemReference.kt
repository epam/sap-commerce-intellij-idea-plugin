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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaRelation
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.ItemResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

/**
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

                // invalid value, can be due `List()List(Product)`
                if (length < 0) return@let super.calculateDefaultRangeInElement()
                TextRange.from(offset, length)
            }
            ?: if (element.textLength == 0) super.calculateDefaultRangeInElement()
            else TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, CngTSItemReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, CngTSItemReference> { ref ->
            val project = ref.project
            val metaModelAccess = TSMetaModelAccess.getInstance(project)

            val name = ref.value
            val result = metaModelAccess.findMetaItemByName(name)
                ?.let { resolve(it) }
                ?: metaModelAccess.findMetaEnumByName(name)
                    ?.let { resolve(it) }
                ?: metaModelAccess.findMetaRelationByName(name)
                    ?.let { resolve(it) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                project.service<TSModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT
            )
        }

        private fun resolve(meta: TSGlobalMetaItem): Array<ResolveResult> = meta.declarations
            .map { ItemResolveResult(it) }
            .toTypedArray()

        private fun resolve(meta: TSGlobalMetaEnum): Array<ResolveResult> = meta.declarations
            .map { EnumResolveResult(it) }
            .toTypedArray()

        private fun resolve(meta: TSGlobalMetaRelation): Array<ResolveResult> = meta.declarations
            .map { RelationResolveResult(it) }
            .toTypedArray()

        private val regexes = arrayOf(
            // https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9b5366ff6eb34df5be29881ff55f97d2/8bad5c918669101499c8d4802cd12214.html
            "^Range\\((.*)\\)\$".toRegex(),
            // https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9b5366ff6eb34df5be29881ff55f97d2/8c0606208669101480e1b95aa5146259.html
            "^Localized\\((.*)\\)\$".toRegex(),
            // https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9b5366ff6eb34df5be29881ff55f97d2/8c062ff286691014b2618f676f8099fe.html
            "^LocalizedSimple\\((.*)\\)\$".toRegex(),
            // https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9b5366ff6eb34df5be29881ff55f97d2/8bac73cc866910148c4bcba41953b0ed.html
            "^List\\((.*)\\)\$".toRegex(),
            // https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9b5366ff6eb34df5be29881ff55f97d2/8bad8ae286691014bf4ab06b9b99d7c6.html
            "^Reference\\((.*)\\)\$".toRegex(),
            // https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9b5366ff6eb34df5be29881ff55f97d2/f7ce138967d0470aaeae5c7e01e1c162.html
            "^FixedValuesReference\\((.*)\\)\$".toRegex(),
            // https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9b5366ff6eb34df5be29881ff55f97d2/8bad002786691014924ba694583a1368.html
            "^MultiReference-(COLLECTION|LIST|SET)\\((.*)\\)\$".toRegex(),
            // https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9b5366ff6eb34df5be29881ff55f97d2/8bab997f86691014852cfbeabd21a5c8.html
            "^ExtendedMultiReference-(COLLECTION|LIST|SET)\\((.*)\\)\$".toRegex(),
            // https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9b5366ff6eb34df5be29881ff55f97d2/8bab6c4486691014a3899cab8989d996.html
            "^EnumMultiReference-(COLLECTION|LIST|SET)\\((.*)\\)\$".toRegex(),
        )
    }

}
