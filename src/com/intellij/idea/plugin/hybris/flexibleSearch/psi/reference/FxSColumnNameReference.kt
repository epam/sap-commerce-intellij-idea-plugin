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
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.FlexibleSearchCompletionContributor
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.reference.result.FxSColumnAliasNameResolveResult
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.reference.result.FxSYColumnNameResolveResult
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class FxSColumnNameReference(owner: FlexibleSearchColumnName) : PsiReferenceBase.Poly<FlexibleSearchColumnName>(owner) {

    override fun calculateDefaultRangeInElement(): TextRange {
        val originalType = element.text
        val type = FxSPsiUtils.getTableAliasName(element.text)
        return TextRange.from(originalType.indexOf(type), type.length)
    }

    override fun handleElementRename(newElementName: String) = element.setName(newElementName)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    override fun getVariants() = PsiTreeUtil.getPrevSiblingOfType(element, FlexibleSearchSelectedTableName::class.java)
        ?.reference
        ?.resolve()
        ?.parent
        ?.let { fromClause ->
            val aliases = findColumnAliasNames(fromClause) { true }
                .map { FxSLookupElementFactory.build(it) }
            val nonAliasedColumns = findYColumnNames(fromClause) { true }
                .map { FxSLookupElementFactory.build(it) }

            return@let aliases + nonAliasedColumns
        }
        ?.toTypedArray()
        ?: getAlternativeVariants(element)

    companion object {
        val CACHE_KEY =
            Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxSColumnNameReference>>("HYBRIS_FXS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxSColumnNameReference> { ref ->
            val lookingForName = ref.element.text
                .replace(FlexibleSearchCompletionContributor.DUMMY_IDENTIFIER, "")
                .trim()

            val result: Array<ResolveResult> = PsiTreeUtil.getPrevSiblingOfType(ref.element, FlexibleSearchSelectedTableName::class.java)
                ?.reference
                ?.resolve()
                ?.parent
                ?.let { fromClause ->
                    val aliases = findColumnAliasNames(fromClause) { alias -> alias.text.trim() == lookingForName }
                        .map { FxSColumnAliasNameResolveResult(it) }
                    if (aliases.isEmpty()) {
                        return@let findYColumnNames(fromClause) { alias -> alias.text.trim() == lookingForName }
                            .map { FxSYColumnNameResolveResult(it) }
                    } else {
                        return@let aliases
                    }
                }
                ?.toTypedArray()
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }

        private fun findColumnAliasNames(
            fromClause: PsiElement,
            filter: (FlexibleSearchColumnAliasName) -> Boolean
        ) = PsiTreeUtil.findChildrenOfType(fromClause, FlexibleSearchColumnAliasName::class.java)
            .filter { PsiTreeUtil.getParentOfType(it, FlexibleSearchWhereClause::class.java) == null }
            .filter(filter)

        private fun findYColumnNames(
            fromClause: PsiElement,
            filter: (FlexibleSearchYColumnName) -> Boolean
        ) = PsiTreeUtil.findChildrenOfType(fromClause, FlexibleSearchResultColumn::class.java)
            .asSequence()
            .filter { it.childrenOfType<FlexibleSearchColumnAliasName>().isEmpty() }
            .filter { PsiTreeUtil.getParentOfType(it, FlexibleSearchWhereClause::class.java) == null }
            .filter { it.expression is FlexibleSearchColumnRefYExpression }
            .mapNotNull { PsiTreeUtil.findChildOfType(it, FlexibleSearchYColumnName::class.java) }
            .filter(filter)
            .toList()

        private fun getAlternativeVariants(element: PsiElement): Array<LookupElementBuilder> {
            val fxsSettings = HybrisProjectSettingsComponent.getInstance(element.project).state.flexibleSearchSettings

            val addComma = FxSPsiUtils.shouldAddCommaAfterResultColumn(element, fxsSettings)

            return PsiTreeUtil.getParentOfType(element, FlexibleSearchSelectCoreSelect::class.java)
                ?.childrenOfType<FlexibleSearchFromClause>()
                ?.firstOrNull()
                ?.fromClauseExprList
                ?.filterIsInstance<FlexibleSearchFromClauseSelect>()
                ?.mapNotNull { it.fromClauseSubqueries }
                ?.map {
                    val tableAliasName = it.tableAliasName
                    if (tableAliasName != null) {
                        return@map setOf(FxSLookupElementFactory.build(tableAliasName, addComma, fxsSettings))
                    } else {
                        // TODO: not sure what to do if there is no table alias
                        return@map setOf<LookupElementBuilder>()
                    }
                }
                ?.flatten()
                ?.toTypedArray()
                ?: emptyArray()
        }
    }

}