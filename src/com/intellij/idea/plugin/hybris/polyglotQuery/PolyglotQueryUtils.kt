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

package com.intellij.idea.plugin.hybris.polyglotQuery

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiPolyadicExpression
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiVariable
import com.intellij.psi.impl.JavaConstantExpressionEvaluator

object PolyglotQueryUtils {

    private val keywordsRegex = "(GET )"
        .toRegex(RegexOption.IGNORE_CASE)
    private val bracesRegex = ".*\\{.*}.*".toRegex()
    private val whitespaceRegex = "\\s+".toRegex()

    fun isPolyglotQuery(expression: String) = expression.replace("\n", "")
        .replace("\"\"\"", "")
        .trim()
        .startsWith("GET ", true)
        && expression.contains(keywordsRegex)
        && expression.contains(bracesRegex)

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
                            if (value is String || value is Char) {
                                computedValue += value;
                            }
                        }
                    }
                } else {
                    val value = JavaConstantExpressionEvaluator.computeConstantExpression(operand, true);
                    if (value is String || value is Char) {
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