/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

@file:JvmName("FlexibleSearchPsiUtil")

package com.intellij.idea.plugin.hybris.flexibleSearch.psi

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.impl.FlexibleSearchYColumnNameMixin
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import org.jetbrains.plugins.groovy.lang.psi.util.backwardSiblings

fun getPresentationText(resultColumn: FlexibleSearchResultColumn) = (
    resultColumn.childrenOfType<FlexibleSearchColumnAliasName>()
        .firstOrNull()
        ?.text
        ?: resultColumn.childrenOfType<FlexibleSearchFunctionName>()
            .firstOrNull()
            ?.text
        ?: resultColumn.childrenOfType<FlexibleSearchCaseExpression>()
            .firstOrNull()
            ?.let { "<CASE>" }
        ?: resultColumn.childrenOfType<FlexibleSearchColumnRefExpression>()
            .firstOrNull()
            ?.columnName
            ?.name
        ?: resultColumn.childrenOfType<FlexibleSearchFunctionCallExpression>()
            .firstOrNull()
            ?.childrenOfType<FlexibleSearchFunctionName>()
            ?.firstOrNull()
            ?.text
            ?.let { "<$it>" }
        ?: PsiTreeUtil.findChildOfType(resultColumn, FlexibleSearchYColumnNameMixin::class.java)
            ?.text
    )
    ?.trim()

fun setName(element: FlexibleSearchPsiNamedElement, newName: String): PsiElement {
    val identifierNode = element.node.findChildByType(FlexibleSearchTypes.IDENTIFIER)
        ?: element.node.findChildByType(FlexibleSearchTypes.BACKTICK_LITERAL)

    if (identifierNode != null) {
        FlexibleSearchElementFactory.createIdentifier(element.project, newName)
            .firstChild
            .node
            .let { element.node.replaceChild(identifierNode, it) }
    }

    return element
}

fun getName(element: FlexibleSearchPsiNamedElement): String? = element.text
fun getNameIdentifier(element: FlexibleSearchPsiNamedElement): PsiElement = element

fun getTable(element: FlexibleSearchTableAliasName) = element.backwardSiblings()
    .firstOrNull { it is FlexibleSearchDefinedTableName } as? FlexibleSearchDefinedTableName

fun getTableToAlias(element: FlexibleSearchYColumnName): Pair<FlexibleSearchDefinedTableName, FlexibleSearchTableAliasName?>? {
    val tableAlias = element
        .backwardSiblings()
        .firstOrNull { it is FlexibleSearchSelectedTableName }
        ?.reference
        ?.resolve()
        ?.let { it as? FlexibleSearchTableAliasName }

    return tableAlias
        ?.table
        ?.let { it to tableAlias }
        ?: getSuitableTableContainerParent(element)
            .firstOrNull()
            ?.let { fromClause ->
                val definedTableName = PsiTreeUtil.findChildOfType(fromClause, FlexibleSearchDefinedTableName::class.java)
                    ?: return@let null

                definedTableName to definedTableName.tableAlias
            }
}

fun getTableAliases(element: PsiElement): Collection<FlexibleSearchTableAliasName> {
    // Order clause is outside the select core
    if (PsiTreeUtil.getParentOfType(element, FlexibleSearchOrderClause::class.java) != null) {
        return PsiTreeUtil.getParentOfType(element, FlexibleSearchSelectStatement::class.java)
            ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java) }
            ?: emptyList()
    }

    // Where a case also may contain sub-queries, in such a case visibility to aliases will be from top-most available select
    val topWhereClauseTableAliases = PsiTreeUtil.getTopmostParentOfType(element, FlexibleSearchWhereClause::class.java)
        ?.let { topWhereClause ->
            PsiTreeUtil.getParentOfType(topWhereClause, FlexibleSearchSelectCoreSelect::class.java)
                ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java) }
        }
        ?: emptyList()
    // Case when we're in the Result column, we may have nested selects in the result column, so have to find the top one
    val topResultColumnsTableAliases = PsiTreeUtil.getTopmostParentOfType(element, FlexibleSearchResultColumns::class.java)
        ?.let { topResultColumns ->
            PsiTreeUtil.getParentOfType(topResultColumns, FlexibleSearchSelectStatement::class.java)
                ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java) }
        }
        ?: emptyList()

    val tableAliases = topWhereClauseTableAliases + topResultColumnsTableAliases
    if (tableAliases.isNotEmpty()) return tableAliases

    // all other cases, like GROUP BY, HAVING, etc
    return PsiTreeUtil.getParentOfType(element, FlexibleSearchSelectCoreSelect::class.java)
        ?.fromClause
        ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java) }
        ?: emptyList()
}

/*
 Order clause is not part of the CoreSelect, so we have to go upper to Statement itself
 ORDER BY can be in the sub-query, so let's check for it first
 */
private fun getSuitableTableContainerParent(element: PsiElement) = PsiTreeUtil
    .getParentOfType(element, FlexibleSearchOrderClause::class.java)
    ?.parentOfType<FlexibleSearchSelectStatement>()
    ?.selectCoreSelectList
    ?.mapNotNull { it.fromClause }
    ?: PsiTreeUtil
        .getParentOfType(element, FlexibleSearchSelectCoreSelect::class.java)
        ?.fromClause
        ?.let { listOf(it) }
    ?: PsiTreeUtil
        .getParentOfType(element, FlexibleSearchSelectStatement::class.java)
        ?.selectCoreSelectList
        ?.mapNotNull { it.fromClause }
    ?: emptyList()

fun getTableName(element: FlexibleSearchDefinedTableName): String = element.firstChild.text

fun getTableAlias(element: FlexibleSearchDefinedTableName): FlexibleSearchTableAliasName? = element
    .siblings()
    .firstOrNull { it is FlexibleSearchTableAliasName }
    ?.let { it as? FlexibleSearchTableAliasName }
