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
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.flexibleSearch.psi.*
import sap.commerce.toolset.i18n

@Service(Service.Level.PROJECT)
class FlexibleSearchEditorService(
    private val project: Project,
    private val coroutineScope: CoroutineScope
) {

    fun introduceBindParameters(psiFile: FlexibleSearchPsiFile, splitEditor: FlexibleSearchSplitEditorEx) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            // Phase 1: collect literal expressions, create seed virtual parameters with original values
            val usedNames = mutableMapOf<String, Int>()
            val bindParameters = readAction {
                PsiTreeUtil.collectElementsOfType(psiFile, FlexibleSearchLiteralExpression::class.java)
                    .filter { it.bindParameter == null }
                    .sortedBy { it.textOffset }
                    .map {
                        BindParameter(
                            literal = it,
                            name = resolveBindParamName(it, usedNames),
                            rawValue = extractLiteralValue(it)
                        )
                    }
            }
                .takeIf { it.isNotEmpty() }
                ?.sortedByDescending { it.literal.textOffset }
                ?: run {
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
            val newVirtualParameters = writeCommandAction(project, i18n("hybris.fxs.actions.introduce_bind_parameters")) {
                bindParameters
                    .filter { it.literal.isValid }
                    .mapNotNull { currentBindParameter ->
                        val newBindParameter = FlexibleSearchElementFactory.createBindParameterLiteral(project, currentBindParameter.name)
                            ?: return@mapNotNull null

                        currentBindParameter.literal.replace(newBindParameter)
                        currentBindParameter.name to FlexibleSearchVirtualParameter.of(newBindParameter, currentBindParameter.rawValue)
                    }
                    .toMap()
            }

            // Phase 3: build seed map, then create proper instances via of() — picks up operand/rawType from
            // actual bind parameter PSI; existing user-entered values take priority over seeds
            val existingVirtualParameters = splitEditor.virtualParameters ?: emptyMap()
            val mergedVirtualParameters = existingVirtualParameters + newVirtualParameters

            splitEditor.virtualParameters = readAction {
                PsiTreeUtil.collectElementsOfType(psiFile, FlexibleSearchBindParameter::class.java)
                    .associate { bindParam ->
                        bindParam.value to FlexibleSearchVirtualParameter.of(bindParam, mergedVirtualParameters)
                    }
            }

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

    private fun resolveBindParamName(literal: FlexibleSearchLiteralExpression, usedNames: MutableMap<String, Int>): String {
        val baseName = extractColumnName(literal) ?: "param"
        val count = usedNames.getOrDefault(baseName, 0)
        usedNames[baseName] = count + 1
        return if (count == 0) baseName else "$baseName$count"
    }

    private fun extractColumnName(literal: FlexibleSearchLiteralExpression): String? {
        val columnExpr = when (val parent = literal.parent) {
            is FlexibleSearchEquivalenceExpression -> findColumnRef(parent.expressionList)
            is FlexibleSearchComparisonExpression -> findColumnRef(parent.expressionList)
            is FlexibleSearchLikeExpression -> findColumnRef(parent.expressionList)
            is FlexibleSearchInExpression -> findColumnRef(parent.expressionList)
            else -> null
        }
        return columnExpr?.columnRefName()
    }

    private fun findColumnRef(expressions: List<FlexibleSearchExpression>): FlexibleSearchExpression? = expressions
        .filterIsInstance<FlexibleSearchColumnRefYExpression>().firstOrNull()
        ?: expressions.filterIsInstance<FlexibleSearchColumnRefExpression>().firstOrNull()

    private fun FlexibleSearchExpression.columnRefName(): String? = when (this) {
        is FlexibleSearchColumnRefYExpression -> {
            val alias = selectedTableName?.name
            val column = yColumnName?.text
            if (alias != null && column != null) "${alias}_$column"
            else column
        }

        is FlexibleSearchColumnRefExpression -> {
            val alias = selectedTableName?.name
            val column = columnName?.name
            if (alias != null && column != null) "${alias}_$column"
            else column
        }

        else -> null
    }

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

    private data class BindParameter(
        val literal: FlexibleSearchLiteralExpression,
        val name: String,
        val rawValue: String?,
    )

    companion object {
        fun getInstance(project: Project): FlexibleSearchEditorService = project.service()
    }
}
