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

package com.intellij.idea.plugin.hybris.flexibleSearch.psi.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_SOURCE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_TARGET
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.FlexibleSearchCompletionContributor
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchYColumnName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FxSPsiUtils
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.TSCompletionService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class FxSYColumnReference(owner: FlexibleSearchYColumnName) : PsiReferenceBase.Poly<FlexibleSearchYColumnName>(owner) {

    override fun calculateDefaultRangeInElement(): TextRange {
        val originalType = element.text
        val type = FxSPsiUtils.getColumnName(element.text)
        return TextRange.from(originalType.indexOf(type), type.length)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    /*
    By default, Lexer will create non-aliased Element, so we may extend variants with supported aliases first
     */
    override fun getVariants() = getType()
        ?.let { type ->
            val variants = TSCompletionService.getInstance(element.project).getCompletions(type)
                .toTypedArray()

            variants + getPostfixes(type)
        }
        ?: getSuitablePrefixes()

    // no need to add localized postfix if there is already one
    private fun getPostfixes(type: String) = if (element.parent.text.contains("[")) {
        emptyArray()
    } else {
        val text = element.text.replace(FlexibleSearchCompletionContributor.DUMMY_IDENTIFIER, "")
        element.text.substringAfter(FlexibleSearchCompletionContributor.DUMMY_IDENTIFIER, "")
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
    private fun getSuitablePrefixes(): Array<out Any> {
        val fxsSettings = HybrisProjectSettingsComponent.getInstance(element.project).state.flexibleSearchSettings
        val aliasText = element.text.replace(FlexibleSearchCompletionContributor.DUMMY_IDENTIFIER, "")

        val separators: Array<LookupElementBuilder> = element.text.substringAfter(FlexibleSearchCompletionContributor.DUMMY_IDENTIFIER)
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

    fun getType() = element.table
        ?.tableName

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxSYColumnReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxSYColumnReference> { ref ->
            val featureName = FxSPsiUtils.getColumnName(ref.element.text)
            val result = ref.element.table
                ?.tableName
                ?.let { resolve(ref.element.project, it, featureName) }
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                TSMetaModelAccess.getInstance(ref.element.project).getMetaModel(), PsiModificationTracker.MODIFICATION_COUNT
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
                    val attributes = meta.allAttributes
                        .filter { refName.equals(it.name, true) }
                        .map { AttributeResolveResult(it) }

                    val relations = meta.allRelationEnds
                        .filter { refName.equals(it.name, true) }
                        .map { RelationEndResolveResult(it) }

                    (attributes + relations).toTypedArray()
                }

        private fun tryResolveByRelationType(type: String, refName: String, metaService: TSMetaModelAccess): Array<ResolveResult>? {
            val meta = metaService.findMetaRelationByName(type) ?: return null

            if (ATTRIBUTE_SOURCE.equals(refName, true)) {
                return arrayOf(RelationEndResolveResult(meta.source))
            } else if (ATTRIBUTE_TARGET.equals(refName, true)) {
                return arrayOf(RelationEndResolveResult(meta.target))
            }

            return metaService.findMetaItemByName(HybrisConstants.TS_TYPE_LINK)
                ?.allAttributes
                ?.find { refName.equals(it.name, true) }
                ?.let { arrayOf(AttributeResolveResult(it)) }
        }

        private fun tryResolveByEnumType(type: String, refName: String, metaService: TSMetaModelAccess): Array<ResolveResult>? {
            val meta = metaService.findMetaEnumByName(type) ?: return null

            return if (HybrisConstants.ENUM_ATTRIBUTES.contains(refName.lowercase())) {
                arrayOf(EnumResolveResult(meta))
            } else return null
        }

    }

}
