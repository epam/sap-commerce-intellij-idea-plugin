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
package com.intellij.idea.plugin.hybris.flexibleSearch.lang.folding

import ai.grazie.utils.toDistinctTypedArray
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFile
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil

class FlexibleSearchFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val foldingSettings = HybrisProjectSettingsComponent.getInstance(root.project).state.flexibleSearchSettings.folding
        if (!foldingSettings.enabled) {
            return emptyArray()
        }

        return CachedValuesManager.getCachedValue(root) {
            val filter = ApplicationManager.getApplication().getService(FlexibleSearchFoldingBlocksFilter::class.java)
            val results = SyntaxTraverser.psiTraverser(root)
                .filter { filter.isAccepted(it) }
                .mapNotNull {
                    if (it is PsiErrorElement || it.textRange.isEmpty) return@mapNotNull null
                    FoldingDescriptor(it.node, it.textRange, FoldingGroup.newGroup(GROUP_NAME))
                }
                .toDistinctTypedArray()

            CachedValueProvider.Result.create(
                results,
                root.containingFile,
                ProjectRootModificationTracker.getInstance(root.project),
                foldingSettings
            )
        }
    }

    override fun getPlaceholderText(node: ASTNode) = when (node.elementType) {
        FlexibleSearchTypes.COMMENT -> "/*...*/"

        FlexibleSearchTypes.COLUMN_REF_Y_EXPRESSION -> getColumnPlaceholderText(
            node,
            FlexibleSearchTypes.Y_COLUMN_NAME,
            FlexibleSearchTypes.SELECTED_TABLE_NAME
        )

        FlexibleSearchTypes.COLUMN_REF_EXPRESSION -> node.findChildByType(FlexibleSearchTypes.COLUMN_NAME)
            ?.text
            ?.trim()

        FlexibleSearchTypes.FROM_TABLE -> node.findChildByType(FlexibleSearchTypes.DEFINED_TABLE_NAME)
            ?.text
            ?.trim()

        FlexibleSearchTypes.SELECT_CORE_SELECT -> {
            val coreSelect = node.psi as? FlexibleSearchSelectCoreSelect
            val columns = coreSelect
                ?.resultColumns
                ?.resultColumnList
                ?.joinToString { it.presentationText ?: "?" }
                ?.takeIf { it.isNotBlank() }
                ?.trim()
                ?: "?"

            val tables = coreSelect
                ?.fromClause
                ?.fromClauseExprList
                ?.map {
                    when (it) {
                        is FlexibleSearchFromClauseSelect -> it.tableAliasName
                            ?.name
                            ?: it.fromClauseSubqueries
                                ?.tableAliasName
                                ?.name

                        is FlexibleSearchYFromClause -> it.fromClauseSimple
                            ?.let { that ->
                                PsiTreeUtil.findChildOfType(that, FlexibleSearchDefinedTableName::class.java)
                                    ?.tableName
                            }

                        else -> null
                    }
                }
                ?.joinToString { it ?: "?" }
                ?.let { if (it.contains(",")) "[$it]" else it }

            "$tables($columns)"
        }

        else -> FALLBACK_PLACEHOLDER
    }
        ?: FALLBACK_PLACEHOLDER

    private fun getColumnPlaceholderText(node: ASTNode, columnNameType: IElementType, tableAliasType: IElementType): String {
        val fxsSettings = HybrisProjectSettingsComponent.getInstance(node.psi.project).state.flexibleSearchSettings
        val columnName = node.findChildByType(columnNameType)
            ?.text
            ?.trim()
            ?: "?"

        val alias = fxsSettings.folding.showSelectedTableNameForYColumn
            .takeIf { it }
            ?.let { node.findChildByType(tableAliasType) }
            ?.text
            ?.trim()
            ?.let { it + fxsSettings.completion.defaultTableAliasSeparator }
            ?: ""

        val language = fxsSettings.folding.showLanguageForYColumn
            .takeIf { it }
            ?.let { node.findChildByType(FlexibleSearchTypes.COLUMN_LOCALIZED_NAME) }
            ?.text
            ?.trim()
            ?.let { ":$it" }
            ?: ""

        return alias + columnName + language
    }

    override fun isCollapsedByDefault(node: ASTNode) = node.psi.parent.parent !is FlexibleSearchFile

    companion object {
        private const val GROUP_NAME = "FlexibleSearch"
        private const val FALLBACK_PLACEHOLDER = "..."
    }
}
