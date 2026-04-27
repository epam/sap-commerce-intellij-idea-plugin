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

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind
import com.intellij.codeInsight.codeVision.CodeVisionEntry
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering
import com.intellij.codeInsight.codeVision.ui.model.ClickableRichTextCodeVisionEntry
import com.intellij.codeInsight.codeVision.ui.model.richText.RichText
import com.intellij.codeInsight.daemon.impl.JavaCodeVisionProviderBase
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.settings.language.isInlaySettingsEditor
import com.intellij.ide.actions.FqnUtil
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.awt.RelativePoint
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.logging.CxLogConstants
import sap.commerce.toolset.logging.CxRemoteLogStateService
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import java.awt.event.MouseEvent
import java.util.*

class CxLoggerInlayHintsProvider : JavaCodeVisionProviderBase() {

    override val defaultAnchor: CodeVisionAnchorKind = CodeVisionAnchorKind.Default
    override val id: String = "SAPCxLoggerInlayHintsProvider"
    override val name: String = "SAP CX Logger"
    override val relativeOrderings: List<CodeVisionRelativeOrdering> = emptyList()

    private data class LoggerHintTarget(
        val element: PsiElement,
        val loggerIdentifier: String,
        val kind: LoggerHintKind,
        val overriddenByField: LoggerFieldInfo? = null,
    )

    private enum class LoggerHintKind {
        PACKAGE,
        CLASS,
        FIELD,
    }

    private data class LoggerFieldInfo(
        val field: PsiField,
        val loggerIdentifier: String,
    )

    override fun computeLenses(editor: Editor, psiFile: PsiFile): List<Pair<TextRange, CodeVisionEntry>> {
        if (psiFile.isNotHybrisProject) return emptyList()
        val project = psiFile.project

        return collectHintTargets(psiFile, editor)
            .map { target ->
                val range = InlayHintsUtils.getTextRangeWithoutLeadingCommentsAndWhitespaces(target.element)
                val logger = CxRemoteLogStateService.getInstance(project).logger(target.loggerIdentifier)
                if (target.kind == LoggerHintKind.CLASS && shouldSuppressClassHint(target, logger, editor)) {
                    return@map null
                }
                val text = buildHintText(logger, target.overriddenByField != null)
                val handler = ClickHandler(target.element, target.loggerIdentifier)
                val tooltip = buildTooltip(logger, target.overriddenByField)

                return@map range to ClickableRichTextCodeVisionEntry(id, text, handler, HybrisIcons.Y.REMOTE, "", tooltip)
            }
            .filterNotNull()
    }

    private inner class ClickHandler(
        element: PsiElement,
        private val loggerIdentifier: String,
    ) : (MouseEvent?, Editor) -> Unit {
        private val elementPointer = SmartPointerManager.createPointer(element)

        override fun invoke(event: MouseEvent?, editor: Editor) {
            if (isInlaySettingsEditor(editor)) return
            elementPointer.element ?: return

            handleClick(editor, loggerIdentifier, event)
        }
    }

    fun handleClick(editor: Editor, loggerIdentifier: String, event: MouseEvent?) {
        val actionGroup = ActionManager.getInstance().getAction("sap.cx.logging.actions") as ActionGroup
        val project = editor.project ?: return
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.EDITOR, editor)
            .add(CxLogConstants.DATA_KEY_LOGGER_IDENTIFIER, loggerIdentifier)
            .build()

        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            /* title = */ "Select an Option",
            /* actionGroup = */ actionGroup,
            /* dataContext = */ dataContext,
            /* selectionAidMethod = */ JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            /* showDisabledActions = */ true
        )

        // Convert the point to a RelativePoint
        val relativePoint = if (event != null) RelativePoint(event)
        else JBPopupFactory.getInstance().guessBestPopupLocation(editor)

        // Show the popup at the calculated relative point
        popup.show(relativePoint)
    }

    private fun collectHintTargets(psiFile: PsiFile, editor: Editor): List<LoggerHintTarget> {
        return PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiClass::class.java, PsiPackageStatement::class.java, PsiField::class.java)
            .mapNotNull { psiElement ->
                when (psiElement) {
                    is PsiClass -> createClassHintTarget(psiElement, editor)
                    is PsiPackageStatement -> LoggerHintTarget(psiElement, psiElement.packageName, LoggerHintKind.PACKAGE)
                    is PsiField -> createFieldHintTarget(psiElement)
                    else -> null
                }
            }
    }

    private fun createClassHintTarget(psiClass: PsiClass, editor: Editor): LoggerHintTarget? {
        val classIdentifier = psiClass.nameIdentifier ?: return null
        val classFqn = FqnUtil.elementToFqn(psiClass, editor) ?: return null
        val loggerFields = psiClass.allFields
            .asSequence()
            .mapNotNull { resolveLoggerFieldInfo(it, psiClass) }
            .toList()
        val overridingField = loggerFields.firstOrNull { it.loggerIdentifier != classFqn }

        return LoggerHintTarget(classIdentifier, classFqn, LoggerHintKind.CLASS, overridingField)
    }

    private fun createFieldHintTarget(field: PsiField): LoggerHintTarget? {
        val contextClass = field.containingClass ?: return null
        val loggerFieldInfo = resolveLoggerFieldInfo(field, contextClass) ?: return null

        return LoggerHintTarget(field, loggerFieldInfo.loggerIdentifier, LoggerHintKind.FIELD)
    }

    private fun shouldSuppressClassHint(
        target: LoggerHintTarget,
        logger: CxLoggerPresentation?,
        editor: Editor,
    ): Boolean {
        logger ?: return false
        if (target.overriddenByField != null) return true
        val psiClass = target.element.parent as? PsiClass ?: return false
        val classFqn = FqnUtil.elementToFqn(psiClass, editor) ?: return false
        val hasLoggerField = psiClass.allFields
            .asSequence()
            .mapNotNull { resolveLoggerFieldInfo(it, psiClass) }
            .any()

        if (target.loggerIdentifier != classFqn) return false

        return when {
            !logger.inherited -> false
            hasLoggerField -> true
            else -> true
        }
    }

    private fun buildHintText(logger: CxLoggerPresentation?, overridden: Boolean): RichText {
        if (logger == null) return RichText("[y] log level")

        val style = when {
            overridden -> SimpleTextAttributes(
                SimpleTextAttributes.STYLE_STRIKEOUT or SimpleTextAttributes.STYLE_BOLD,
                JBColor.RED
            )

            logger.inherited -> SimpleTextAttributes(
                SimpleTextAttributes.STYLE_UNDERLINE or SimpleTextAttributes.STYLE_BOLD or SimpleTextAttributes.STYLE_ITALIC,
                JBColor.GRAY
            )

            else -> SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.blue)
        }

        return RichText("[").apply {
            append(logger.level.name, style)
            append("] log level", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
    }

    private fun buildTooltip(logger: CxLoggerPresentation?, overriddenByField: LoggerFieldInfo?): String = when {
        overriddenByField != null -> "Overridden by logger field '${overriddenByField.field.name}' with identifier '${overriddenByField.loggerIdentifier}'"
        logger == null -> "Fetch or Define the logger for SAP Commerce"
        logger.inherited -> "Inherited from: ${logger.parentName}"
        else -> "Setup the logger for SAP Commerce"
    }

    private fun resolveLoggerFieldInfo(field: PsiField, contextClass: PsiClass): LoggerFieldInfo? {
        val loggerIdentifier = resolveLoggerIdentifier(field, contextClass, mutableSetOf()) ?: return null
        return LoggerFieldInfo(field, loggerIdentifier)
    }

    private fun resolveLoggerIdentifier(field: PsiField, contextClass: PsiClass, visited: MutableSet<PsiElement>): String? {
        if (!visited.add(field)) return null
        val initializer = field.initializer ?: return null
        return resolveLoggerIdentifier(initializer, contextClass, visited)
    }

    private fun resolveLoggerIdentifier(expression: PsiMethodCallExpression, contextClass: PsiClass): String? {
        val methodName = expression.methodExpression.referenceName
        if (methodName !in CxLogConstants.LOGGER_FACTORY_METHOD_NAMES) return null

        val containingClassName = expression.resolveMethod()
            ?.containingClass
            ?.qualifiedName
        if (containingClassName !in CxLogConstants.LOGGER_FACTORY_CLASS_NAMES) return null

        val arguments = expression.argumentList.expressions
        if (arguments.isEmpty()) {
            return contextClass.qualifiedName
        }

        return resolveLoggerIdentifier(arguments.first(), contextClass, mutableSetOf())
    }

    private fun resolveLoggerIdentifier(
        expression: PsiExpression,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): String? = when (expression) {
        is PsiClassObjectAccessExpression -> expression.operand.type.canonicalText
        is PsiParenthesizedExpression -> expression.expression?.let { resolveLoggerIdentifier(it, contextClass, visited) }
        is PsiPolyadicExpression -> resolvePolyadicStringExpression(expression, contextClass, visited)
        else -> evaluateStringConstant(expression)
            ?: resolveReferencedLoggerIdentifier(expression, contextClass, visited)
            ?: if (expression is PsiMethodCallExpression) {
                resolveLoggerIdentifier(expression, contextClass)
                    ?: resolveLoggerIdentifierFromMethodCall(expression, contextClass, visited)
            } else null
    }

    private fun resolveReferencedLoggerIdentifier(
        expression: PsiExpression,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): String? {
        if (expression !is PsiReferenceExpression) return null

        return when (val resolved = expression.resolve()) {
            is PsiClass -> resolved.qualifiedName
            is PsiVariable -> {
                if (!visited.add(resolved)) return null
                val initializer = resolved.initializer ?: return null
                resolveLoggerIdentifier(initializer, contextClass, visited)
            }

            else -> null
        }
    }

    private fun resolveLoggerIdentifierFromMethodCall(
        expression: PsiMethodCallExpression,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): String? {
        return resolveClassReferenceFromMethodCall(expression, contextClass, visited)
            ?: resolveStringMethodCall(expression, contextClass, visited)
            ?: resolveFormattedString(expression, contextClass, visited)
    }

    private fun resolveClassReferenceFromMethodCall(
        expression: PsiMethodCallExpression,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): String? {
        val methodName = expression.methodExpression.referenceName ?: return null
        return when (methodName) {
            "getName", "getCanonicalName", "getTypeName" -> {
                val qualifier = expression.methodExpression.qualifierExpression ?: return null
                resolveLoggerIdentifier(qualifier, contextClass, visited)
            }

            "getSimpleName" -> {
                val qualifier = expression.methodExpression.qualifierExpression ?: return null
                resolveLoggerIdentifier(qualifier, contextClass, visited)
                    ?.substringAfterLast('.')
            }

            "getClass" -> contextClass.qualifiedName
            "getPackageName" -> {
                val qualifier = expression.methodExpression.qualifierExpression ?: return null
                resolveLoggerIdentifier(qualifier, contextClass, visited)
                    ?.substringBeforeLast('.', "")
            }

            else -> null
        }
    }

    private fun resolveFormattedString(
        expression: PsiMethodCallExpression,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): String? {
        val methodName = expression.methodExpression.referenceName ?: return null
        val formatString = when (methodName) {
            "format", "formatted" -> resolveStringLikeValue(expression.methodExpression.qualifierExpression, contextClass, visited)
            else -> null
        } ?: return null

        val rawArguments = expression.argumentList.expressions
        val arguments = when (methodName) {
            "format" -> rawArguments.map { resolveFormatArgument(it, contextClass, visited) }.toTypedArray()
            "formatted" -> rawArguments.map { resolveFormatArgument(it, contextClass, visited) }.toTypedArray()
            else -> emptyArray()
        }

        return runCatching {
            java.lang.String.format(Locale.ROOT, formatString, *arguments)
        }.getOrNull()
    }

    private fun resolveStringMethodCall(
        expression: PsiMethodCallExpression,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): String? {
        val qualifier = expression.methodExpression.qualifierExpression ?: return null
        val base = resolveStringLikeValue(qualifier, contextClass, visited) ?: return null
        val arguments = expression.argumentList.expressions

        return when (expression.methodExpression.referenceName) {
            "replace" -> resolveReplaceCall(base, arguments, contextClass, visited)
            "concat" -> {
                val suffix = arguments.singleOrNull()
                    ?.let { resolveStringLikeValue(it, contextClass, visited) }
                    ?: return null
                base + suffix
            }

            "toLowerCase" -> if (arguments.isEmpty()) base.lowercase(Locale.ROOT) else null
            "toUpperCase" -> if (arguments.isEmpty()) base.uppercase(Locale.ROOT) else null
            "trim" -> if (arguments.isEmpty()) base.trim() else null
            else -> null
        }
    }

    private fun resolveReplaceCall(
        base: String,
        arguments: Array<PsiExpression>,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): String? {
        if (arguments.size != 2) return null

        val oldString = resolveStringLikeValue(arguments[0], contextClass, visited)
        val newString = resolveStringLikeValue(arguments[1], contextClass, visited)
        if (oldString != null && newString != null) {
            return base.replace(oldString, newString)
        }

        val oldChar = resolveCharConstant(arguments[0])
        val newChar = resolveCharConstant(arguments[1])
        if (oldChar != null && newChar != null) {
            return base.replace(oldChar, newChar)
        }

        return null
    }

    private fun resolveCharConstant(expression: PsiExpression): Char? {
        return JavaPsiFacade.getInstance(expression.project).constantEvaluationHelper
            .computeConstantExpression(expression, false) as? Char
    }

    private fun resolvePolyadicStringExpression(
        expression: PsiPolyadicExpression,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): String? {
        if (expression.operationTokenType != JavaTokenType.PLUS) return null

        return expression.operands.fold(StringBuilder()) { acc, operand ->
            val value = resolveStringLikeValue(operand, contextClass, visited) ?: return null
            acc.append(value)
        }.toString()
    }

    private fun resolveStringLikeValue(
        expression: PsiExpression?,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): String? {
        expression ?: return null
        return when (expression) {
            is PsiParenthesizedExpression -> resolveStringLikeValue(expression.expression, contextClass, visited)
            is PsiPolyadicExpression -> resolvePolyadicStringExpression(expression, contextClass, visited)
            else -> evaluateStringConstant(expression)
            ?: resolveReferencedLoggerIdentifier(expression, contextClass, visited)
            ?: if (expression is PsiMethodCallExpression) {
                resolveLoggerIdentifierFromMethodCall(expression, contextClass, visited)
            } else null
        }
    }

    private fun resolveFormatArgument(
        expression: PsiExpression,
        contextClass: PsiClass,
        visited: MutableSet<PsiElement>,
    ): Any? {
        return JavaPsiFacade.getInstance(expression.project).constantEvaluationHelper
            .computeConstantExpression(expression, false)
            ?: resolveLoggerIdentifier(expression, contextClass, visited)
    }

    private fun evaluateStringConstant(expression: PsiExpression): String? {
        return JavaPsiFacade.getInstance(expression.project).constantEvaluationHelper
            .computeConstantExpression(expression, false) as? String
    }
}
