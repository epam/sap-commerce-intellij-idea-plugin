/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.logging.codeInsight.hints

import com.intellij.psi.*
import sap.commerce.toolset.logging.CxLogConstants
import java.util.*

class CxLoggerIdentifierResolver(private val contextClass: PsiClass) {

    private val visited = mutableSetOf<PsiElement>()

    fun resolve(field: PsiField): String? {
        if (!visited.add(field)) return null
        val initializer = field.initializer ?: return null
        return resolveExpression(initializer)
    }

    private fun resolveExpression(expression: PsiExpression): String? {
        val constant = JavaPsiFacade.getInstance(expression.project).constantEvaluationHelper
            .computeConstantExpression(expression, false)
        if (constant is String) return constant

        return when (expression) {
            is PsiClassObjectAccessExpression -> expression.operand.type.canonicalText
            is PsiParenthesizedExpression -> expression.expression?.let { resolveExpression(it) }
            is PsiPolyadicExpression -> resolvePolyadicExpression(expression)
            is PsiReferenceExpression -> resolveReference(expression)
            is PsiMethodCallExpression -> resolveMethodCall(expression)
            else -> null
        }
    }

    private fun resolvePolyadicExpression(expression: PsiPolyadicExpression): String? {
        if (expression.operationTokenType != JavaTokenType.PLUS) return null
        return expression.operands.fold(StringBuilder()) { acc, operand ->
            val value = resolveExpression(operand) ?: return null
            acc.append(value)
        }.toString()
    }

    private fun resolveReference(expression: PsiReferenceExpression): String? {
        return when (val resolved = expression.resolve()) {
            is PsiClass -> resolved.qualifiedName
            is PsiVariable -> {
                if (!visited.add(resolved)) return null
                resolved.initializer?.let { resolveExpression(it) }
            }

            else -> null
        }
    }

    private fun resolveMethodCall(expression: PsiMethodCallExpression): String? {
        val methodName = expression.methodExpression.referenceName ?: return null
        val qualifier = expression.methodExpression.qualifierExpression
        val args = expression.argumentList.expressions

        if (methodName in CxLogConstants.LOGGER_FACTORY_METHOD_NAMES) {
            val containingClass = expression.resolveMethod()?.containingClass?.qualifiedName
                ?: (qualifier as? PsiReferenceExpression)?.resolve()?.let { (it as? PsiClass)?.qualifiedName }
            if (containingClass in CxLogConstants.LOGGER_FACTORY_CLASS_NAMES) {
                return if (args.isEmpty()) contextClass.qualifiedName
                else resolveExpression(args.first())
            }
        }

        when (methodName) {
            "getName", "getCanonicalName", "getTypeName" -> return qualifier?.let { resolveExpression(it) }
            "getSimpleName" -> return qualifier?.let { resolveExpression(it)?.substringAfterLast('.') }
            "getClass" -> return contextClass.qualifiedName
            "getPackageName" -> return qualifier?.let { resolveExpression(it)?.substringBeforeLast('.', "") }
        }

        when (methodName) {
            "format", "formatted" -> {
                val formatStr = qualifier?.let { resolveExpression(it) } ?: return null
                val arguments = args.map { arg ->
                    JavaPsiFacade.getInstance(arg.project).constantEvaluationHelper
                        .computeConstantExpression(arg, false)
                        ?: resolveExpression(arg)
                }.toTypedArray()
                return runCatching { java.lang.String.format(Locale.ROOT, formatStr, *arguments) }.getOrNull()
            }
        }

        val base = qualifier?.let { resolveExpression(it) } ?: return null
        return when (methodName) {
            "replace" -> resolveReplaceCall(base, args)
            "concat" -> args.singleOrNull()?.let { resolveExpression(it) }?.let { base + it }
            "toLowerCase" -> if (args.isEmpty()) base.lowercase(Locale.ROOT) else null
            "toUpperCase" -> if (args.isEmpty()) base.uppercase(Locale.ROOT) else null
            "trim" -> if (args.isEmpty()) base.trim() else null
            else -> null
        }
    }

    private fun resolveReplaceCall(base: String, arguments: Array<PsiExpression>): String? {
        if (arguments.size != 2) return null

        val s1 = resolveExpression(arguments[0])
        val s2 = resolveExpression(arguments[1])
        if (s1 != null && s2 != null) return base.replace(s1, s2)

        val helper = JavaPsiFacade.getInstance(arguments[0].project).constantEvaluationHelper
        val c1 = helper.computeConstantExpression(arguments[0], false) as? Char
        val c2 = helper.computeConstantExpression(arguments[1], false) as? Char
        if (c1 != null && c2 != null) return base.replace(c1, c2)

        return null
    }
}
