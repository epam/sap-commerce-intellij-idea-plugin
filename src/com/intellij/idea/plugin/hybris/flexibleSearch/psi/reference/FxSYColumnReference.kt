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

package com.intellij.idea.plugin.hybris.flexibleSearch.psi.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_SOURCE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_TARGET
import com.intellij.idea.plugin.hybris.flexibleSearch.FxSUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchDefinedTableName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableAliasName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchYColumnName
import com.intellij.idea.plugin.hybris.psi.util.PsiTreeUtilExt
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
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

class FxSYColumnReference(owner: FlexibleSearchYColumnName) : PsiReferenceBase.Poly<FlexibleSearchYColumnName>(owner) {

    override fun calculateDefaultRangeInElement(): TextRange {
        val originalType = element.text
        val type = FxSUtils.getColumnName(element.text)
        return TextRange.from(originalType.indexOf(type), type.length)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    /*
    By default, Lexer will create non-aliased Element, so we may extend variants with supported aliases first
     */
    override fun getVariants(): Array<out Any> {
        val tableToAlias = getTableToAlias()
            ?: return getSuitablePrefixes()
        val type = tableToAlias.first.tableName
        val hasTableAlias = tableToAlias.second != null
        val hasColumnAlias = isAliasedReference()
        val canFallback = canFallbackToTableName()

        if (!hasColumnAlias && HybrisConstants.FXS_DUMMY_IDENTIFIER == element.text && hasTableAlias) {
            return getSuitablePrefixes()
        }

        if (!hasColumnAlias && HybrisConstants.FXS_DUMMY_IDENTIFIER == element.text && !hasTableAlias) {
            return getSuitablePrefixes() + getColumns(type)
        }
        if ((hasColumnAlias && hasTableAlias)
            || (!hasColumnAlias && (!hasTableAlias || canFallback))
        ) {
            return getColumns(type)
        }

        return getSuitablePrefixes()
    }

    private fun getColumns(type: String): Array<LookupElementBuilder> {
        val variants = TSCompletionService.getInstance(element.project).getCompletions(
            type,
            TSMetaType.META_ITEM, TSMetaType.META_ENUM, TSMetaType.META_RELATION
        )
            .toTypedArray()

        return variants + getPostfixes(type)
    }

    // no need to add localized postfix if there is already one
    private fun getPostfixes(type: String): Array<LookupElementBuilder> = if (element.parent.text.contains("[")) {
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

    /*
    If cursor placed at the end of the literal, in addition to table aliases, we will add allowed separators
     */
    private fun getSuitablePrefixes(): Array<LookupElementBuilder> {
        val fxsSettings = DeveloperSettingsComponent.getInstance(element.project).state.flexibleSearchSettings
        val aliasText = element.text.replace(HybrisConstants.FXS_DUMMY_IDENTIFIER, "")

        val separators: Array<LookupElementBuilder> = element.text.substringAfter(HybrisConstants.FXS_DUMMY_IDENTIFIER)
            .takeIf { it.isBlank() && aliasText.isNotBlank() }
            ?.let {
                arrayOf(
                    FxSLookupElementFactory.buildSeparatorDot(aliasText),
                    FxSLookupElementFactory.buildSeparatorColon(aliasText)
                )
            }
            ?: emptyArray()
        val tableAliases: Array<LookupElementBuilder> = element.tableAliases
            .map { FxSLookupElementFactory.build(it, false, fxsSettings) }
            .toTypedArray()

        return separators + tableAliases
    }

    fun getTableToAlias(): Pair<FlexibleSearchDefinedTableName, FlexibleSearchTableAliasName?>? = element.tableToAlias

    fun isAliasedReference() = PsiTreeUtilExt
        .getPrevSiblingOfElementType(element, FlexibleSearchTypes.SELECTED_TABLE_NAME) != null

    fun canFallbackToTableName() = DeveloperSettingsComponent.getInstance(element.project)
        .state
        .flexibleSearchSettings
        .fallbackToTableNameIfNoAliasProvided

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxSYColumnReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxSYColumnReference> { ref ->
            val featureName = FxSUtils.getColumnName(ref.element.text)


            val type = ref.getTableToAlias()
                ?.let {
                    val isAliasedTable = it.second != null
                    val isAliasedReference = ref.isAliasedReference()

                    if (!isAliasedReference && isAliasedTable && !ref.canFallbackToTableName()) {
                        return@let null
                    } else {
                        return@let it.first.tableName
                    }
                }

            val project = ref.element.project
            val result = type
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
                ?: tryResolveByRelationType(type, refName, metaService)
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

        private fun tryResolveByRelationType(type: String, refName: String, metaService: TSMetaModelAccess): Array<ResolveResult>? {
            val meta = metaService.findMetaRelationByName(type) ?: return null

            if (ATTRIBUTE_SOURCE.equals(refName, true)) {
                return arrayOf(RelationEndResolveResult(meta.source))
            } else if (ATTRIBUTE_TARGET.equals(refName, true)) {
                return arrayOf(RelationEndResolveResult(meta.target))
            }

            return tryResolveByItemType(HybrisConstants.TS_TYPE_LINK, refName, metaService)
        }

        private fun tryResolveByEnumType(type: String, refName: String, metaService: TSMetaModelAccess): Array<ResolveResult>? = metaService.findMetaEnumByName(type)
            ?.let { metaService.findMetaItemByName(HybrisConstants.TS_TYPE_ENUMERATION_VALUE) }
            ?.let { it.allAttributes[refName] }
            ?.let { arrayOf(AttributeResolveResult(it)) }

    }

}
