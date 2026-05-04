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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class CxLoggerIdentifierResolver(private val contextClass: PsiClass) {

    private val visited = mutableSetOf<PsiElement>()
    private val cache = ConcurrentHashMap<PsiElement, Any>()

    fun resolve(field: PsiField): String? {
        if (!visited.add(field)) return null
        val initializer = field.initializer ?: return null
        return resolveExpression(initializer)
    }

    private fun resolveExpression(expression: PsiExpression): String? {
        val helper = JavaPsiFacade.getInstance(expression.project).constantEvaluationHelper
        return helper.computeExpression(expression, false, createAuxEvaluator(helper)) as? String
    }

    private fun createAuxEvaluator(helper: PsiConstantEvaluationHelper): PsiConstantEvaluationHelper.AuxEvaluator {
        return object : PsiConstantEvaluationHelper.AuxEvaluator {
            override fun computeExpression(expression: PsiExpression, self: PsiConstantEvaluationHelper.AuxEvaluator): Any? {
                fun resolve(expr: PsiExpression): Any? = helper.computeExpression(expr, false, self)

                return when (expression) {
                    is PsiClassObjectAccessExpression -> expression.operand.type.canonicalText
                    is PsiReferenceExpression -> when (val resolved = expression.resolve()) {
                        is PsiClass -> resolved.qualifiedName
                        is PsiVariable -> {
                            if (!visited.add(resolved)) return null
                            resolved.initializer?.let { resolve(it) }
                        }

                        else -> null
                    }

                    is PsiMethodCallExpression -> resolveMethodCall(expression, ::resolve)
                    else -> null
                }
            }

            override fun getCacheMap(topLevel: Boolean): ConcurrentMap<PsiElement, Any> = cache
        }
    }

    private fun resolveMethodCall(
        expression: PsiMethodCallExpression,
        resolve: (PsiExpression) -> Any?,
    ): Any? {
        val methodName = expression.methodExpression.referenceName ?: return null
        val qualifier = expression.methodExpression.qualifierExpression
        val args = expression.argumentList.expressions

        if (methodName in CxLogConstants.LOGGER_FACTORY_METHOD_NAMES) {
            val containingClass = expression.resolveMethod()?.containingClass?.qualifiedName
            if (containingClass in CxLogConstants.LOGGER_FACTORY_CLASS_NAMES) {
                return if (args.isEmpty()) contextClass.qualifiedName
                else resolve(args.first())
            }
        }

        when (methodName) {
            "getName", "getCanonicalName", "getTypeName" -> return qualifier?.let { resolve(it) }
            "getSimpleName" -> return qualifier?.let { (resolve(it) as? String)?.substringAfterLast('.') }
            "getClass" -> return contextClass.qualifiedName
            "getPackageName" -> return qualifier?.let { (resolve(it) as? String)?.substringBeforeLast('.', "") }
        }

        when (methodName) {
            "format", "formatted" -> {
                val formatStr = qualifier?.let { resolve(it) as? String } ?: return null
                val arguments = args.map { resolve(it) }.toTypedArray()
                return runCatching { java.lang.String.format(Locale.ROOT, formatStr, *arguments) }.getOrNull()
            }
        }

        val base = qualifier?.let { resolve(it) as? String } ?: return null
        return when (methodName) {
            "replace" -> resolveReplaceCall(base, args, resolve)
            "concat" -> args.singleOrNull()?.let { resolve(it) as? String }?.let { base + it }
            "toLowerCase" -> if (args.isEmpty()) base.lowercase(Locale.ROOT) else null
            "toUpperCase" -> if (args.isEmpty()) base.uppercase(Locale.ROOT) else null
            "trim" -> if (args.isEmpty()) base.trim() else null
            else -> null
        }
    }

    private fun resolveReplaceCall(
        base: String,
        arguments: Array<PsiExpression>,
        resolve: (PsiExpression) -> Any?,
    ): String? {
        if (arguments.size != 2) return null

        val first = resolve(arguments[0])
        val second = resolve(arguments[1])

        return when {
            first is String && second is String -> base.replace(first, second)
            first is Char && second is Char -> base.replace(first, second)
            else -> null
        }
    }
}
