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

package sap.commerce.toolset.flexibleSearch.editor

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.flexibleSearch.psi.*
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService

@Service(Service.Level.PROJECT)
class FlexibleSearchEditorService(
    private val project: Project,
    private val coroutineScope: CoroutineScope
) {

    fun introduceBindParameters(psiFile: FlexibleSearchPsiFile, splitEditor: FlexibleSearchSplitEditorEx) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            // Phase 1: collect replacements and seed named parameters with original values
            val collected = readAction { collectReplacements(psiFile) }

            if (collected.replacements.isEmpty()) {
                Notifications
                    .info(
                        i18n("hybris.fxs.actions.introduce_bind_parameters"),
                        i18n("hybris.fxs.actions.introduce_bind_parameters.no_literals")
                    )
                    .hideAfter(10)
                    .notify(project)
                return@launch
            }

            // Phase 2: replace literals with bind parameters (undoable)
            val appliedParameterNames = writeCommandAction(project, i18n("hybris.fxs.actions.introduce_bind_parameters")) {
                collected.replacements
                    .sortedByDescending { it.anchorOffset }
                    .mapNotNull { applyReplacement(it) }
                    .toSet()
            }

            val newVirtualParameters = collected.parameters
                .filterKeys { it in appliedParameterNames }
                .mapValues { (_, seed) ->
                    FlexibleSearchVirtualParameter(
                        name = seed.name,
                        operand = seed.operand,
                    ).apply { rawValue = seed.rawValue }
                }

            // Phase 3: re-create parameters from actual bind parameter PSI — picks up operand/rawType via the
            // Type System meta model, so only when it is ready; existing user-entered values take priority over seeds
            val mergedVirtualParameters = (splitEditor.virtualParameters ?: emptyMap()) + newVirtualParameters

            splitEditor.virtualParameters = if (isTypeSystemInitialized()) readAction {
                PsiTreeUtil.collectElementsOfType(psiFile, FlexibleSearchBindParameter::class.java)
                    .associate { it.value to FlexibleSearchVirtualParameter.of(it, mergedVirtualParameters) }
            } else mergedVirtualParameters

            // Phase 4: open (or refresh) the In-Editor Virtual Parameters panel
            withContext(Dispatchers.EDT) {
                if (splitEditor.inEditorParameters) {
                    splitEditor.refreshParameters()
                } else {
                    splitEditor.inEditorParameters = true
                }
            }
        }
    }

    private fun collectReplacements(psiFile: FlexibleSearchPsiFile): CollectedReplacements {
        val inListCollapses = PsiTreeUtil.collectElementsOfType(psiFile, FlexibleSearchInExpression::class.java)
            .mapNotNull { collapsibleInList(it) }
        val collapseByFirstLiteral = inListCollapses.associateBy { it.literals.first() }
        val collapsedLiterals = inListCollapses.flatMapTo(hashSetOf()) { it.literals }
        val betweenBaseNames = collectBetweenBaseNames(psiFile)

        val collected = CollectedReplacements()

        PsiTreeUtil.collectElementsOfType(psiFile, FlexibleSearchLiteralExpression::class.java)
            .filter { it.bindParameter == null }
            .sortedBy { it.textOffset }
            .forEach { literal ->
                val collapse = collapseByFirstLiteral[literal]
                when {
                    collapse != null -> collected.addInListReplacement(collapse)
                    literal in collapsedLiterals -> Unit
                    else -> collected.addLiteralReplacement(
                        literal = literal,
                        baseName = betweenBaseNames[literal] ?: extractColumnName(literal) ?: FALLBACK_BASE_NAME,
                        rawValue = extractLiteralValue(literal),
                    )
                }
            }

        return collected
    }

    private fun collapsibleInList(expression: FlexibleSearchInExpression): InListCollapse? {
        if (expression.selectSubqueryCombinedList.isNotEmpty() || expression.bindParameterList.isNotEmpty()) return null

        val operands = expression.expressionList
        val literals = operands.drop(1)
            .filterIsInstance<FlexibleSearchLiteralExpression>()
            .takeIf { it.isNotEmpty() && it.size == operands.size - 1 && it.all { literal -> literal.bindParameter == null } }
            ?: return null
        val values = literals.mapNotNull { extractLiteralValue(it) }
            .takeIf { it.size == literals.size }
            ?: return null

        return InListCollapse(
            inExpression = expression,
            literals = literals,
            baseName = findColumnRef(operands.take(1))?.columnRefName() ?: FALLBACK_BASE_NAME,
            rawValue = values.joinToString("\n"),
        )
    }

    private fun collectBetweenBaseNames(psiFile: FlexibleSearchPsiFile): Map<FlexibleSearchLiteralExpression, String> = PsiTreeUtil
        .collectElementsOfType(psiFile, FlexibleSearchBetweenExpression::class.java)
        .mapNotNull { expression ->
            expression.expressionList
                .filterIsInstance<FlexibleSearchLiteralExpression>()
                .filter { it.bindParameter == null }
                .takeIf { it.size == 2 }
                ?.let { literals ->
                    val baseName = findColumnRef(expression.expressionList)?.columnRefName() ?: FALLBACK_BASE_NAME
                    listOf(
                        literals[0] to "${baseName}_from",
                        literals[1] to "${baseName}_to",
                    )
                }
        }
        .flatten()
        .toMap()

    private fun extractColumnName(literal: FlexibleSearchLiteralExpression): String? {
        val columnExpr = when (val parent = literal.parent) {
            is FlexibleSearchEquivalenceExpression -> findColumnRef(parent.expressionList)
            is FlexibleSearchComparisonExpression -> findColumnRef(parent.expressionList)
            is FlexibleSearchLikeExpression -> findColumnRef(parent.expressionList)
            is FlexibleSearchInExpression -> findColumnRef(parent.expressionList)
            is FlexibleSearchBetweenExpression -> findColumnRef(parent.expressionList)
            else -> null
        }
        return columnExpr?.columnRefName()
    }

    private fun findColumnRef(expressions: List<FlexibleSearchExpression>): FlexibleSearchExpression? = expressions
        .firstNotNullOfOrNull { expression ->
            expression.asSafely<FlexibleSearchColumnRefYExpression>()
                ?: PsiTreeUtil.findChildOfType(expression, FlexibleSearchColumnRefYExpression::class.java)
                ?: expression.asSafely<FlexibleSearchColumnRefExpression>()
                ?: PsiTreeUtil.findChildOfType(expression, FlexibleSearchColumnRefExpression::class.java)
        }

    private fun FlexibleSearchExpression.columnRefName(): String? = when (this) {
        is FlexibleSearchColumnRefYExpression -> toBindParameterName(selectedTableName?.text, yColumnName?.text)
        is FlexibleSearchColumnRefExpression -> toBindParameterName(selectedTableName?.text, columnName?.text)
        else -> null
    }

    private fun toBindParameterName(tableAlias: String?, columnName: String?): String? = listOfNotNull(tableAlias, columnName)
        .joinToString("_")
        .map { if (it.isLetterOrDigit()) it else '_' }
        .joinToString("")
        .takeIf { it.isNotBlank() }

    private fun extractLiteralValue(literal: FlexibleSearchLiteralExpression): String? {
        val singleQuote = literal.singleQuoteStringLiteral
        val doubleQuote = literal.doubleQuoteStringLiteral
        val number = literal.signedNumber
        val bool = literal.booleanLiteral

        return when {
            singleQuote != null -> {
                val text = singleQuote.text
                text.substring(1, text.length - 1).replace("''", "'")
            }

            doubleQuote != null -> {
                val text = doubleQuote.text
                text.substring(1, text.length - 1).replace("\"\"", "\"")
            }

            number != null -> number.text
            bool != null -> bool.text
            else -> null  // NULL literal — no pre-population
        }
    }

    private fun applyReplacement(replacement: Replacement): String? = when (replacement) {
        is LiteralReplacement -> replaceLiteral(replacement)
        is InListReplacement -> replaceInList(replacement)
    }

    private fun replaceLiteral(replacement: LiteralReplacement): String? {
        val literal = replacement.literal.takeIf { it.isValid } ?: return null
        val newBindParameter = FlexibleSearchElementFactory.createBindParameterLiteral(project, replacement.parameter.name)
            ?: return null

        literal.replace(newBindParameter)
        return replacement.parameter.name
    }

    private fun replaceInList(replacement: InListReplacement): String? {
        val literals = replacement.literals.takeIf { all -> all.all { it.isValid } } ?: return null
        val inExpression = replacement.inExpression.takeIf { it.isValid } ?: return null
        val newBindParameter = FlexibleSearchElementFactory.createBindParameterLiteral(project, replacement.parameter.name)
            ?: return null

        // collapse `IN ('a', 'b', 'c')` into `IN (?param)`: drop everything after the first literal
        // (commas, whitespace and remaining literals are all direct children of the IN expression),
        // then swap the still-valid first literal for the bind parameter
        if (literals.size > 1) {
            inExpression.deleteChildRange(literals.first().nextSibling, literals.last())
        }
        literals.first().replace(newBindParameter)
        return replacement.parameter.name
    }

    private fun isTypeSystemInitialized(): Boolean = !project.isDisposed
        && !DumbService.isDumb(project)
        && TSMetaModelStateService.getInstance(project).initialized()

    private data class ParameterSeed(
        val name: String,
        val rawValue: String?,
        val operand: IElementType? = null,
    )

    private sealed interface Replacement {
        val anchorOffset: Int
        val parameter: ParameterSeed
    }

    private data class LiteralReplacement(
        val literal: FlexibleSearchLiteralExpression,
        override val anchorOffset: Int,
        override val parameter: ParameterSeed,
    ) : Replacement

    private data class InListReplacement(
        val inExpression: FlexibleSearchInExpression,
        val literals: List<FlexibleSearchLiteralExpression>,
        override val anchorOffset: Int,
        override val parameter: ParameterSeed,
    ) : Replacement

    private data class InListCollapse(
        val inExpression: FlexibleSearchInExpression,
        val literals: List<FlexibleSearchLiteralExpression>,
        val baseName: String,
        val rawValue: String,
    )

    private class CollectedReplacements {

        val replacements = mutableListOf<Replacement>()
        val parameters = linkedMapOf<String, ParameterSeed>()

        fun addLiteralReplacement(literal: FlexibleSearchLiteralExpression, baseName: String, rawValue: String?) {
            replacements += LiteralReplacement(
                literal = literal,
                anchorOffset = literal.textOffset,
                parameter = resolveParameter(baseName, rawValue),
            )
        }

        fun addInListReplacement(collapse: InListCollapse) {
            replacements += InListReplacement(
                inExpression = collapse.inExpression,
                literals = collapse.literals,
                anchorOffset = collapse.literals.first().textOffset,
                parameter = resolveParameter(collapse.baseName, collapse.rawValue, FlexibleSearchTypes.IN_EXPRESSION),
            )
        }

        // same base name + same value -> reuse the parameter, so one input drives all occurrences;
        // NULL values never merge — each occurrence keeps its own parameter
        private fun resolveParameter(baseName: String, rawValue: String?, operand: IElementType? = null): ParameterSeed {
            var name = baseName
            var index = 0
            while (true) {
                val existing = parameters[name] ?: break
                if (rawValue != null && existing.rawValue == rawValue && existing.operand == operand) return existing
                name = "$baseName${++index}"
            }
            return ParameterSeed(name, rawValue, operand).also { parameters[name] = it }
        }
    }

    companion object {
        private const val FALLBACK_BASE_NAME = "param"

        fun getInstance(project: Project): FlexibleSearchEditorService = project.service()
    }
}
