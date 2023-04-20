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
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser

class FlexibleSearchFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val filter = ApplicationManager.getApplication().getService(FlexibleSearchFoldingBlocksFilter::class.java)

        return SyntaxTraverser.psiTraverser(root)
            .filter { filter.isAccepted(it) }
            .map { FoldingDescriptor(it.node, it.textRange, FoldingGroup.newGroup(GROUP_NAME)) }
            .toDistinctTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode) = when (node.elementType) {
        FlexibleSearchTypes.COLUMN_REF_Y_EXPRESSION,
        FlexibleSearchTypes.COLUMN_REF_EXPRESSION -> node.findChildByType(FlexibleSearchTypes.COLUMN_NAME)
            ?.text
            ?.trim()

        FlexibleSearchTypes.FROM_TABLE -> node.findChildByType(FlexibleSearchTypes.DEFINED_TABLE_NAME)
            ?.text
            ?.trim()

        else -> FALLBACK_PLACEHOLDER
    }
        ?: FALLBACK_PLACEHOLDER

    override fun isCollapsedByDefault(node: ASTNode) = true

    companion object {
        private const val GROUP_NAME = "FlexibleSearch"
        private const val FALLBACK_PLACEHOLDER = "..."
    }
}
