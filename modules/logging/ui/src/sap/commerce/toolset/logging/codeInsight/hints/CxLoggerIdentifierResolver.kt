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

@Service(Service.Level.PROJECT)
class CxLoggerIdentifierResolver(private val project: Project) {

    fun resolve(field: PsiField): String? {
        val contextClass = field.containingClass ?: return null
        val initializer = field.initializer ?: return null
        if (!isLoggerInitializer(initializer, mutableSetOf())) return null
        return resolveExpression(initializer, contextClass, mutableSetOf())
    }

    private fun isLoggerInitializer(expression: PsiExpression, visited: MutableSet<PsiElement>): Boolean = when (expression) {
        is PsiMethodCallExpression -> isLoggerFactoryCall(expression)
        is PsiReferenceExpression -> expression.resolve()
            ?.asSafely<PsiVariable>()
            ?.takeIf { visited.add(it) }
            ?.initializer
            ?.let { isLoggerInitializer(it, visited) }
            ?: false

        else -> false
    }

    private fun isLoggerFactoryCall(expression: PsiMethodCallExpression): Boolean {
        if (expression.methodExpression.referenceName !in CxLogConstants.LOGGER_FACTORY_METHOD_NAMES) return false
        val containingClass = expression.resolveMethod()?.containingClass?.qualifiedName
            ?: expression.methodExpression.qualifierExpression.asSafely<PsiReferenceExpression>()
                ?.resolve()
                ?.asSafely<PsiClass>()
                ?.qualifiedName
        return containingClass in CxLogConstants.LOGGER_FACTORY_CLASS_NAMES
    }

    private fun resolveExpression(expression: PsiExpression, contextClass: PsiClass, visited: MutableSet<PsiElement>): String? {
        val constant = JavaPsiFacade.getInstance(project).constantEvaluationHelper.computeConstantExpression(expression, false)
        if (constant is String) return constant

        return when (expression) {
            is PsiClassObjectAccessExpression -> expression.operand.type.canonicalText
            is PsiReferenceExpression -> resolveReference(expression, contextClass, visited)
            is PsiMethodCallExpression -> resolveMethodCall(expression, contextClass, visited)
            else -> null
        }
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

        val base by lazy { qualifier?.let { resolveExpression(it, contextClass, visited) } }

        return when (methodName) {
            in CxLogConstants.LOGGER_FACTORY_METHOD_NAMES -> {
                if (!isLoggerFactoryCall(expression)) return null
                if (args.isEmpty()) contextClass.qualifiedName
                else resolveExpression(args.first(), contextClass, visited)
            }
            "getName", "getCanonicalName", "getTypeName" -> base
            "getSimpleName" -> base?.substringAfterLast('.')
            "getClass" -> contextClass.qualifiedName
            "getPackageName" -> base?.substringBeforeLast('.', "")
            else -> null
        }
    }

    companion object {
        fun getInstance(project: Project): CxLoggerIdentifierResolver = project.service()
    }
}
