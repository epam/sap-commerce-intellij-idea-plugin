/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.formatting

import com.intellij.formatting.Alignment
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.annotations.Contract
import sap.commerce.toolset.impex.psi.ImpExFile
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.impex.psi.ImpExUserRightsValueGroup
import sap.commerce.toolset.impex.psi.ImpExValueGroup
import sap.commerce.toolset.impex.utils.ImpExPsiUtils

open class ImpExColumnsAlignmentStrategy : ImpExAlignmentStrategy {

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

        return alignment
    }

    override fun processNode(currentNode: ASTNode) {
        if (isStartOfTheFile(currentNode)) {
            columnNumber = 0
            alignments.clear()
            return
        }

        if (isNewLine(currentNode)) {
            columnNumber = 0
        }

        if (isHeaderLine(currentNode)) {
            alignments.clear()
        }

        if (ImpExPsiUtils.isUserRightsMacros(currentNode.psi)) {
            alignments.clear()
        }
    }

    @Contract(pure = true)
    fun isStartOfTheFile(currentNode: ASTNode) = currentNode.psi is ImpExFile

    @Contract(pure = true)
    open fun isNewLine(currentNode: ASTNode) = isNewColumn(currentNode)
        && isStartOfValueLine(currentNode)

    @Contract(pure = true)
    open fun isNewColumn(currentNode: ASTNode) = ImpExTypes.VALUE_GROUP == currentNode.elementType
        || ImpExTypes.USER_RIGHTS_VALUE_GROUP == currentNode.elementType

    @Contract(pure = true)
    fun isStartOfValueLine(currentNode: ASTNode) = PsiTreeUtil
        .findChildOfAnyType(
            currentNode.treeParent.psi,
            ImpExValueGroup::class.java,
            ImpExUserRightsValueGroup::class.java
        ) == currentNode.psi

    @Contract(pure = true)
    fun isHeaderLine(currentNode: ASTNode) = ImpExTypes.HEADER_LINE == currentNode.elementType
        || ImpExTypes.USER_RIGHTS_HEADER_LINE == currentNode.elementType
}
