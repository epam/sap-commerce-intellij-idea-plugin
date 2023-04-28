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
fun getNameIdentifier(element: FlexibleSearchPsiNamedElement): PsiElement? = element

fun getTable(element: FlexibleSearchTableAliasName) = element.backwardSiblings()
    .firstOrNull { it is FlexibleSearchDefinedTableName } as? FlexibleSearchDefinedTableName

fun getTable(element: FlexibleSearchYColumnName): FlexibleSearchDefinedTableName? {
    val tableAlias = element.backwardSiblings()
        .firstOrNull { it is FlexibleSearchSelectedTableName }
        ?.reference
        ?.resolve() as? FlexibleSearchTableAliasName
    return tableAlias
        ?.table
        ?: getSuitableTableContainerParent(element)
            .firstOrNull()
            ?.let { fromClause ->
                val definedTableName = PsiTreeUtil.findChildOfType(fromClause, FlexibleSearchDefinedTableName::class.java)

                val definedTableAlias = definedTableName
                    ?.siblings()
                    ?.firstOrNull { it is FlexibleSearchTableAliasName }

                if (definedTableAlias == null) {
                    definedTableName
                } else null
            }
}

fun getTableAliases(element: FlexibleSearchYColumnName): List<FlexibleSearchTableAliasName> {
    val tableAliases = mutableListOf<FlexibleSearchTableAliasName>()
    var fromClauses = getSuitableTableContainerParent(element)

    fromClauses
        .forEach { tableAliases.addAll(getAliases(it)) }

    while (fromClauses.isNotEmpty()) {
        val firstFromClause = fromClauses.first()
        val subQueryParent = firstFromClause.parentOfType<FlexibleSearchSelectSubqueryCombined>()
            ?: break

        fromClauses = getSuitableTableContainerParent(subQueryParent)

        fromClauses
            .forEach { tableAliases.addAll(getAliases(it)) }
    }
    return tableAliases
        // allow only aliases with tables
        .filter { it.table != null }
}

private fun getAliases(element: FlexibleSearchFromClause) = PsiTreeUtil
    .findChildrenOfType(element, FlexibleSearchTableAliasName::class.java)
    .filter { it.parentOfType<FlexibleSearchFromClause>() == element }

/*
 Order clause is not part of the CoreSelect, so we have to go upper to Statement itself
 */
private fun getSuitableTableContainerParent(element: PsiElement) = PsiTreeUtil
    .getParentOfType(element, FlexibleSearchSelectCoreSelect::class.java)
    ?.fromClause
    ?.let { listOf(it) }
    ?: PsiTreeUtil
        .getParentOfType(element, FlexibleSearchSelectStatement::class.java)
        ?.selectCoreSelectList
        ?.mapNotNull { it.fromClause }
    ?: emptyList()

fun getTableName(element: FlexibleSearchDefinedTableName): String = element.firstChild.text
