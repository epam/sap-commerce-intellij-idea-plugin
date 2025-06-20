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

package com.intellij.idea.plugin.hybris.acl.formatting

import com.intellij.formatting.Alignment
import com.intellij.idea.plugin.hybris.acl.psi.AclFile
import com.intellij.idea.plugin.hybris.acl.psi.AclTypes
import com.intellij.lang.ASTNode

class AclAlignmentStrategy {

    private val alignments = mutableListOf<Alignment>()
    private var columnNumber = 0

    fun getAlignment(currentNode: ASTNode): Alignment {
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

    fun processNode(currentNode: ASTNode) {
        if (currentNode.psi is AclFile) {
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
    }

    fun isNewLine(currentNode: ASTNode) = isStartOfValueLine(currentNode)

    fun isNewColumn(currentNode: ASTNode) = AclTypes.PARAMETERS_SEPARATOR == currentNode.elementType
        || AclTypes.FIELD_VALUE_SEPARATOR == currentNode.elementType
        || AclTypes.FIELD_VALUE_TYPE_SEPARATOR == currentNode.elementType

    fun isStartOfValueLine(currentNode: ASTNode) = AclTypes.USER_RIGHTS_VALUE_LINE_PASSWORD_AWARE == currentNode.elementType
        || AclTypes.USER_RIGHTS_VALUE_LINE_PASSWORD_UNAWARE == currentNode.elementType
        || AclTypes.USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_AWARE == currentNode.elementType
        || AclTypes.USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_UNAWARE == currentNode.elementType

    fun isHeaderLine(currentNode: ASTNode) = AclTypes.USER_RIGHTS_HEADER_LINE_PASSWORD_AWARE == currentNode.elementType
        || AclTypes.USER_RIGHTS_HEADER_LINE_PASSWORD_UNAWARE == currentNode.elementType
}