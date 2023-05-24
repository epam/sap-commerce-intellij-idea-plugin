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

package com.intellij.idea.plugin.hybris.flexibleSearch

import com.intellij.idea.plugin.hybris.flexibleSearch.completion.FlexibleSearchCompletionContributor.Companion.DUMMY_IDENTIFIER
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchGroupByClause
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchOrderClause
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchResultColumns
import com.intellij.idea.plugin.hybris.flexibleSearch.settings.FlexibleSearchSettings
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.impl.JavaConstantExpressionEvaluator
import com.intellij.psi.util.PsiTreeUtil

object FxSUtils {

    private val fxsKeywordsRegex = "(SELECT )|( UNION )|( DISTINCT )|( ORDER BY )|( LEFT JOIN )|( JOIN )|( FROM )|( WHERE )|( ASC )|( DESC )|( ON )"
        .toRegex(RegexOption.IGNORE_CASE)
    private val fxsBracesRegex = ".*\\{.*}.*".toRegex()
    private val whitespaceRegex = "\\s+".toRegex()

    fun getColumnName(text: String) = text
        .replace("`", "")
        .trim()

    fun getTableAliasName(text: String) = text
        .replace("`", "")
        .trim()

    fun shouldAddCommaAfterExpression(element: PsiElement, fxsSettings: FlexibleSearchSettings): Boolean {
        var addComma = false
        if (fxsSettings.completion.injectCommaAfterExpression && element.text == DUMMY_IDENTIFIER) {
            addComma = PsiTreeUtil
                .getParentOfType(
                    element,
                    FlexibleSearchResultColumns::class.java,
                    FlexibleSearchOrderClause::class.java,
                    FlexibleSearchGroupByClause::class.java,
                )
                ?.text
                ?.substringAfter(DUMMY_IDENTIFIER)
                ?.trim()
                ?.takeUnless { it.startsWith(",") }
                ?.isNotEmpty()
                ?: false
        }
        return addComma
    }

    fun isFlexibleSearchQuery(expression: String) = expression.contains(fxsKeywordsRegex) && expression.contains(fxsBracesRegex)

    fun computeExpression(element: PsiLiteralExpression): String = if (element.isTextBlock) {
        element.text
            .replace("\"\"\"", "")
    } else {
        StringUtil.unquoteString(element.text)
    }

    fun computeExpression(literalExpression: PsiPolyadicExpression): String {
        var computedValue = ""

        literalExpression.operands
            .forEach { operand ->
                if (operand is PsiReference) {
                    val probableDefinition = operand.resolve()
                    if (probableDefinition is PsiVariable) {
                        probableDefinition.initializer?.let { initializer ->
                            val value = JavaConstantExpressionEvaluator.computeConstantExpression(initializer, true);
                            if (value is String) {
                                computedValue += value;
                            }
                        }
                    }
                } else {
                    val value = JavaConstantExpressionEvaluator.computeConstantExpression(operand, true);
                    if (value is String) {
                        computedValue += value;
                    }
                }
            }
        return computedValue
            .trim()
            .replace("\n", "")
            .replace("\t", "")
            .replace(whitespaceRegex, " ")
    }

}