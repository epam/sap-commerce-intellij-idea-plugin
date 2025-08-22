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
import sap.commerce.toolset.flexibleSearch.FxSUtils
import sap.commerce.toolset.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchSelectedTableName
import sap.commerce.toolset.flexibleSearch.psi.reference.result.FxSTableAliasNameResolveResult
import sap.commerce.toolset.psi.getValidResults

class FxSSelectedTableNameReference(owner: FlexibleSearchSelectedTableName) : PsiReferenceBase.Poly<FlexibleSearchSelectedTableName>(owner) {

    override fun calculateDefaultRangeInElement(): TextRange {
        val originalType = element.text
        val type = FxSUtils.getTableAliasName(element.text)
        return TextRange.from(originalType.indexOf(type), type.length)
    }

    override fun handleElementRename(newElementName: String) = element.setName(newElementName)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { getValidResults(it) }

    override fun getVariants() = element.tableAliases
        .map { tableAlias -> FxSLookupElementFactory.build(tableAlias) }
        .toTypedArray()

    companion object {
        val CACHE_KEY =
            Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxSSelectedTableNameReference>>("HYBRIS_FXS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxSSelectedTableNameReference> { ref ->
            val lookingForName = ref.element.text
                .replace(FlexibleSearchConstants.DUMMY_IDENTIFIER, "")
                .trim()

            val result: Array<ResolveResult> = ref.element.tableAliases
                .firstOrNull { alias -> alias.text.trim() == lookingForName }
                ?.let { arrayOf(FxSTableAliasNameResolveResult(it)) }
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }

//        private fun getTableAliases(element: PsiElement): Collection<FlexibleSearchTableAliasName> {
//            // Order clause is outside the select core
//            if (PsiTreeUtil.getParentOfType(element, FlexibleSearchOrderClause::class.java) != null) {
//                return PsiTreeUtil.getParentOfType(element, FlexibleSearchSelectStatement::class.java)
//                    ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java) }
//                    ?: emptyList()
//            }
//
//            // Where case also may contain sub-queries, in such a case visibility to aliases will be from top-most available select
//            val topWhereClauseTableAliases = PsiTreeUtil.getTopmostParentOfType(element, FlexibleSearchWhereClause::class.java)
//                ?.let { topWhereClause ->
//                    PsiTreeUtil.getParentOfType(topWhereClause, FlexibleSearchSelectCoreSelect::class.java)
//                        ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java) }
//                }
//                ?: emptyList()
//            // Case when we're in the Result column, we may have nested selects in the result column, so have to find top one
//            val topResultColumnsTableAliases = PsiTreeUtil.getTopmostParentOfType(element, FlexibleSearchResultColumns::class.java)
//                ?.let { topResultColumns ->
//                    PsiTreeUtil.getParentOfType(topResultColumns, FlexibleSearchSelectStatement::class.java)
//                        ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java) }
//                }
//                ?: emptyList()
//
//            val tableAliases = topWhereClauseTableAliases + topResultColumnsTableAliases
//            if (tableAliases.isNotEmpty()) return tableAliases
//
//            // all other cases, like GROUP BY, HAVING, etc
//            return PsiTreeUtil.getParentOfType(element, FlexibleSearchSelectCoreSelect::class.java)
//                ?.fromClause
//                ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java) }
//                ?: emptyList()
//        }

    }

}