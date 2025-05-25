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

package com.intellij.idea.plugin.hybris.polyglotQuery.psi.reference

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.flexibleSearch.FxSUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryAttributeKeyName
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.TSCompletionService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.OrderingAttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class PolyglotQueryAttributeKeyNameReference(owner: PolyglotQueryAttributeKeyName) : PsiReferenceBase.Poly<PolyglotQueryAttributeKeyName>(owner) {

    override fun calculateDefaultRangeInElement() = TextRange.from(0, element.text.length)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    override fun getVariants() = element.typeName
        ?.let { type ->
            val variants = TSCompletionService.getInstance(element.project).getCompletions(
                type,
                TSMetaType.META_ITEM, TSMetaType.META_ENUM
            )
                .toTypedArray()

            variants + getPostfixes(type)
        }
        ?: emptyArray()

    // no need to add localized postfix if there is already one
    private fun getPostfixes(type: String) = if (element.parent.text.contains("[")) {
        emptyArray()
    } else {
        val text = element.text.replace(HybrisConstants.FXS_DUMMY_IDENTIFIER, "")
        element.text.substringAfter(HybrisConstants.FXS_DUMMY_IDENTIFIER, "")
            .takeIf { it.isBlank() && text.isNotBlank() }
            ?.let {
                resolve(element.project, type, text)
                    .firstOrNull()
                    ?.let { result ->
                        FxSLookupElementFactory.tryBuildLocalizedName(result, text)
                            ?.let { arrayOf(it) }
                    }
            }
            ?: emptyArray()
    }

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, PolyglotQueryAttributeKeyNameReference>>("HYBRIS_PGQ_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, PolyglotQueryAttributeKeyNameReference> { ref ->
            val featureName = FxSUtils.getColumnName(ref.element.text)
            val project = ref.element.project
            val result = ref.element.typeName
                ?.let { resolve(project, it, featureName) }
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                project.service<TSModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT
            )
        }

        private fun resolve(project: Project, type: String, refName: String): Array<ResolveResult> {
            val metaService = TSMetaModelAccess.getInstance(project)
            return tryResolveByItemType(type, refName, metaService)
                ?: tryResolveByEnumType(type, refName, metaService)
                ?: ResolveResult.EMPTY_ARRAY
        }

        private fun tryResolveByItemType(type: String, refName: String, metaService: TSMetaModelAccess): Array<ResolveResult>? =
            metaService.findMetaItemByName(type)
                ?.let { meta ->
                    val attributes = meta.allAttributes[refName]
                        ?.let { AttributeResolveResult(it) }
                        ?.let { listOf(it) }
                        ?: emptyList()

                    val orderingAttributes = meta.allOrderingAttributes[refName]
                        ?.let { OrderingAttributeResolveResult(it) }
                        ?.let { listOf(it) }
                        ?: emptyList()

                    val relations = meta.allRelationEnds
                        .filter { refName.equals(it.name, true) }
                        .map { RelationEndResolveResult(it) }

                    (attributes + orderingAttributes + relations).toTypedArray()
                }

        private fun tryResolveByEnumType(type: String, refName: String, metaService: TSMetaModelAccess): Array<ResolveResult>? = metaService.findMetaEnumByName(type)
            ?.let { metaService.findMetaItemByName(HybrisConstants.TS_TYPE_ENUMERATION_VALUE) }
            ?.let { it.allAttributes[refName] }
            ?.let { arrayOf(AttributeResolveResult(it)) }

    }

}
