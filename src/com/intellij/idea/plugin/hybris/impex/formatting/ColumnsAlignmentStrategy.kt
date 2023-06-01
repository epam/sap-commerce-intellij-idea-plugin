/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.impex.formatting;

import com.intellij.formatting.Alignment
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRightsValueGroup
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueGroup
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.annotations.Contract

open class ColumnsAlignmentStrategy : AlignmentStrategy {

    private val alignments: MutableList<Alignment> = mutableListOf()
    private var columnNumber = 0

    override fun getAlignment(currentNode: ASTNode): Alignment {
        if (!isNewColumn(currentNode)) return Alignment.createAlignment()

        val alignment: Alignment
        if (columnNumber >= alignments.size) {
            alignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT)
            alignments.add(alignment)
        } else {
            alignment = alignments[columnNumber]
        }
        columnNumber++

        return alignment;
    }

    override fun processNode(currentNode: ASTNode) {
        if (isStartOfTheFile(currentNode)) {
            columnNumber = 0
            alignments.clear()
            return
        }

        when {
            isNewLine(currentNode) -> columnNumber = 0

            isHeaderLine(currentNode) -> alignments.clear()

            ImpexPsiUtils.isUserRightsMacros(currentNode.psi) -> alignments.clear()
        }
    }

    @Contract(pure = true)
    fun isStartOfTheFile(currentNode: ASTNode) = currentNode.psi is ImpexFile

    @Contract(pure = true)
    open fun isNewLine(currentNode: ASTNode) = isNewColumn(currentNode)
        && isStartOfValueLine(currentNode);

    @Contract(pure = true)
    open fun isNewColumn(currentNode: ASTNode) = ImpexTypes.VALUE_GROUP == currentNode.elementType
        || ImpexTypes.USER_RIGHTS_VALUE_GROUP == currentNode.elementType

    @Contract(pure = true)
    fun isStartOfValueLine(currentNode: ASTNode) = PsiTreeUtil
        .findChildOfAnyType(
            currentNode.treeParent.psi,
            ImpexValueGroup::class.java,
            ImpexUserRightsValueGroup::class.java
        ) == currentNode.psi

    @Contract(pure = true)
    fun isHeaderLine(currentNode: ASTNode) = ImpexTypes.HEADER_LINE == currentNode.elementType
        || ImpexTypes.USER_RIGHTS_HEADER_LINE == currentNode.elementType
}
