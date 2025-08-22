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

package sap.commerce.toolset.flexibleSearch.psi.reference

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import sap.commerce.toolset.flexibleSearch.FlexibleSearchConstants
import sap.commerce.toolset.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchDefinedTableName
import sap.commerce.toolset.psi.getValidResults
import sap.commerce.toolset.typeSystem.codeInsight.completion.TSCompletionService
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaEnum
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaRelation
import sap.commerce.toolset.typeSystem.meta.model.TSMetaType
import sap.commerce.toolset.typeSystem.psi.reference.result.EnumResolveResult
import sap.commerce.toolset.typeSystem.psi.reference.result.ItemResolveResult
import sap.commerce.toolset.typeSystem.psi.reference.result.RelationResolveResult

class FxSDefinedTableReference(owner: FlexibleSearchDefinedTableName) : PsiReferenceBase.Poly<FlexibleSearchDefinedTableName>(owner) {

    override fun calculateDefaultRangeInElement(): TextRange {
        val originalType = element.text
        val type = element.tableName
        return TextRange.from(originalType.indexOf(type), type.length)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { getValidResults(it) }

    override fun getVariants(): Array<out Any> {
        val aliasText = element.text.replace(FlexibleSearchConstants.DUMMY_IDENTIFIER, "")
        val suffixes = element.text.substringAfter(FlexibleSearchConstants.DUMMY_IDENTIFIER)
            .takeIf { it.isBlank() && aliasText.isNotBlank() }
            ?.let {
                arrayOf(
                    FxSLookupElementFactory.buildTablePostfixExclamationMark(aliasText),
                    FxSLookupElementFactory.buildTablePostfixStar(aliasText)
                )
            }
            ?: emptyArray()

        val types = TSCompletionService.getInstance(element.project)
            .getCompletions(TSMetaType.META_ITEM, TSMetaType.META_ENUM, TSMetaType.META_RELATION)
            .toTypedArray()

        return suffixes + types
    }

    companion object {
        val CACHE_KEY =
            Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxSDefinedTableReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxSDefinedTableReference> { ref ->
            val lookingForName = ref.element.tableName
            val project = ref.element.project

            val results: Array<ResolveResult> = TSMetaModelAccess.getInstance(project).findMetaClassifierByName(lookingForName)
                ?.let {
                    when (it) {
                        is TSGlobalMetaItem -> it.declarations.map { meta -> ItemResolveResult(meta) }
                        is TSGlobalMetaEnum -> it.declarations.map { meta -> EnumResolveResult(meta) }
                        is TSGlobalMetaRelation -> it.declarations.map { meta -> RelationResolveResult(meta) }
                        else -> null
                    }
                }
                ?.toTypedArray()
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                results,
                TSModificationTracker.getInstance(project), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}