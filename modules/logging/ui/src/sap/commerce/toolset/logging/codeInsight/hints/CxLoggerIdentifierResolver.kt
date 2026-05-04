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

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.util.asSafely
import sap.commerce.toolset.logging.CxLogConstants
import java.util.*

@Service(Service.Level.PROJECT)
class CxLoggerIdentifierResolver(private val project: Project) {

    fun resolve(field: PsiField): String? {
        val contextClass = field.containingClass ?: return null
        val initializer = field.initializer ?: return null
        val visited = mutableSetOf<PsiElement>()

        if (!visited.add(field)) return null

        return resolveExpression(initializer, contextClass, visited)
    }

    private fun resolveExpression(expression: PsiExpression, contextClass: PsiClass, visited: MutableSet<PsiElement>): String? {
        val constant = JavaPsiFacade.getInstance(project).constantEvaluationHelper
            .computeConstantExpression(expression, false)
        if (constant is String) return constant

        return when (expression) {
            is PsiClassObjectAccessExpression -> expression.operand.type.canonicalText
            is PsiParenthesizedExpression -> expression.expression?.let { resolveExpression(it, contextClass, visited) }
            is PsiPolyadicExpression -> resolvePolyadicExpression(expression, contextClass, visited)
            is PsiReferenceExpression -> resolveReference(expression, contextClass, visited)
            is PsiMethodCallExpression -> resolveMethodCall(expression, contextClass, visited)
            else -> null
        }
    }

    private fun resolvePolyadicExpression(expression: PsiPolyadicExpression, contextClass: PsiClass, visited: MutableSet<PsiElement>): String? {
        if (expression.operationTokenType != JavaTokenType.PLUS) return null
        return expression.operands.fold(StringBuilder()) { acc, operand ->
            val value = resolveExpression(operand, contextClass, visited) ?: return null
            acc.append(value)
        }.toString()
    }

    private fun resolveReference(expression: PsiReferenceExpression, contextClass: PsiClass, visited: MutableSet<PsiElement>): String? {
        return when (val resolved = expression.resolve()) {
            is PsiClass -> resolved.qualifiedName
            is PsiVariable -> {
                if (!visited.add(resolved)) return null
                resolved.initializer?.let { resolveExpression(it, contextClass, visited) }
            }

            else -> null
        }
    }

    private fun resolveMethodCall(expression: PsiMethodCallExpression, contextClass: PsiClass, visited: MutableSet<PsiElement>): String? {
        val methodName = expression.methodExpression.referenceName ?: return null
        val qualifier = expression.methodExpression.qualifierExpression
        val args = expression.argumentList.expressions

        if (methodName in CxLogConstants.LOGGER_FACTORY_METHOD_NAMES) {
            val containingClass = expression.resolveMethod()?.containingClass?.qualifiedName
                ?: qualifier.asSafely<PsiReferenceExpression>()
                    ?.resolve()
                    ?.asSafely<PsiClass>()
                    ?.qualifiedName
            if (containingClass in CxLogConstants.LOGGER_FACTORY_CLASS_NAMES) {
                return if (args.isEmpty()) contextClass.qualifiedName
                else resolveExpression(args.first(), contextClass, visited)
            }
        }

        when (methodName) {
            "getName", "getCanonicalName", "getTypeName" -> return qualifier?.let { resolveExpression(it, contextClass, visited) }
            "getSimpleName" -> return qualifier?.let { resolveExpression(it, contextClass, visited)?.substringAfterLast('.') }
            "getClass" -> return contextClass.qualifiedName
            "getPackageName" -> return qualifier?.let { resolveExpression(it, contextClass, visited)?.substringBeforeLast('.', "") }
        }

        when (methodName) {
            "format", "formatted" -> {
                val formatStr = qualifier?.let { resolveExpression(it, contextClass, visited) } ?: return null
                val arguments = args.map { arg ->
                    JavaPsiFacade.getInstance(project).constantEvaluationHelper
                        .computeConstantExpression(arg, false)
                        ?: resolveExpression(arg, contextClass, visited)
                }.toTypedArray()
                return runCatching { java.lang.String.format(Locale.ROOT, formatStr, *arguments) }.getOrNull()
            }
        }

        val base = qualifier?.let { resolveExpression(it, contextClass, visited) } ?: return null
        return when (methodName) {
            "replace" -> resolveReplaceCall(base, args, contextClass, visited)
            "concat" -> args.singleOrNull()?.let { resolveExpression(it, contextClass, visited) }?.let { base + it }
            "toLowerCase" -> if (args.isEmpty()) base.lowercase(Locale.ROOT) else null
            "toUpperCase" -> if (args.isEmpty()) base.uppercase(Locale.ROOT) else null
            "trim" -> if (args.isEmpty()) base.trim() else null
            else -> null
        }
    }

    private fun resolveReplaceCall(base: String, arguments: Array<PsiExpression>, contextClass: PsiClass, visited: MutableSet<PsiElement>): String? {
        if (arguments.size != 2) return null

        val s1 = resolveExpression(arguments[0], contextClass, visited)
        val s2 = resolveExpression(arguments[1], contextClass, visited)
        if (s1 != null && s2 != null) return base.replace(s1, s2)

        val helper = JavaPsiFacade.getInstance(project).constantEvaluationHelper
        val c1 = helper.computeConstantExpression(arguments[0], false).asSafely<Char>()
        val c2 = helper.computeConstantExpression(arguments[1], false).asSafely<Char>()
        if (c1 != null && c2 != null) return base.replace(c1, c2)

        return null
    }

    companion object {
        fun getInstance(project: Project): CxLoggerIdentifierResolver = project.service()
    }
}
