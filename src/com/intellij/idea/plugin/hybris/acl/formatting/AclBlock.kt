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

import com.intellij.formatting.*
import com.intellij.idea.plugin.hybris.acl.psi.AclTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock

class AclBlock(
    node: ASTNode,
    wrap: Wrap? = Wrap.createWrap(WrapType.NONE, false),
    alignment: Alignment? = null,
    private val spacingBuilder: SpacingBuilder,
    private val codeStyleSettings: CodeStyleSettings,
    private val alignmentStrategy: AclAlignmentStrategy
) : AbstractBlock(node, wrap, alignment) {

    override fun getDebugName() = when (myNode.elementType) {
        AclTypes.PARAMETERS_SEPARATOR -> "Parameter Separator"
        AclTypes.FIELD_VALUE_SEPARATOR -> "Field Separator"
        AclTypes.LINE_COMMENT -> "Line Comment"
        AclTypes.START_USERRIGHTS -> "User Rights - Start"
        AclTypes.END_USERRIGHTS -> "User Rights - End"
        AclTypes.USER_RIGHTS_HEADER_LINE_PASSWORD_AWARE -> "Header Line - Password Aware"
        AclTypes.USER_RIGHTS_HEADER_LINE_PASSWORD_UNAWARE -> "Header Line - Password Unaware"
        AclTypes.USER_RIGHTS_HEADER_PARAMETER_TYPE -> "Header - Type"
        AclTypes.USER_RIGHTS_HEADER_PARAMETER_UID -> "Header - UID"
        AclTypes.USER_RIGHTS_HEADER_PARAMETER_MEMBER_OF_GROUPS -> "Header - Member Of Group"
        AclTypes.USER_RIGHTS_HEADER_PARAMETER_PASSWORD -> "Header - Password"
        AclTypes.USER_RIGHTS_HEADER_PARAMETER_TARGET -> "Header - Target"
        AclTypes.USER_RIGHTS_HEADER_PARAMETER_PERMISSION -> "Header - Permission"
        AclTypes.USER_RIGHTS_VALUE_LINE_PASSWORD_AWARE -> "Value Line - Password Aware"
        AclTypes.USER_RIGHTS_VALUE_LINE_PASSWORD_UNAWARE -> "Value Line - Password Unaware"
        AclTypes.USER_RIGHTS_VALUE_GROUP_TYPE -> "Group - Type"
        AclTypes.USER_RIGHTS_VALUE_GROUP_UID -> "Group - UID"
        AclTypes.USER_RIGHTS_VALUE_GROUP_MEMBER_OF_GROUPS -> "Group - Member Of Group"
        AclTypes.USER_RIGHTS_VALUE_GROUP_PASSWORD -> "Group - Password"
        AclTypes.USER_RIGHTS_VALUE_GROUP_TARGET -> "Group - Target"
        AclTypes.USER_RIGHTS_VALUE_GROUP_PERMISSION -> "Group - Permission"
        AclTypes.FIELD_VALUE_TYPE -> "Type"
        AclTypes.FIELD_VALUE -> "Value"
        AclTypes.COMMA -> "Comma"
        AclTypes.DOT -> "Dot"
        AclTypes.PERMISSION_DENIED -> "Permission - Denied"
        AclTypes.PERMISSION_GRANTED -> "Permission - Granted"
        AclTypes.PERMISSION_INHERITED -> "Permission - Inherited"

        else -> "Block"
    }

    override fun isLeaf() = myNode.firstChildNode == null
    override fun getSpacing(child1: Block?, child2: Block): Spacing? = spacingBuilder.getSpacing(this, child1, child2)
    override fun getIndent(): Indent = Indent.getNoneIndent()

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()

        alignmentStrategy.processNode(myNode)

        var currentNode = myNode.firstChildNode

        while (currentNode != null) {
            alignmentStrategy.processNode(currentNode)

            if (isNewBlockToBeMade(currentNode)) {
                val block = AclBlock(
                    node = currentNode,
                    alignment = alignmentStrategy.getAlignment(currentNode),
                    spacingBuilder = spacingBuilder,
                    codeStyleSettings = codeStyleSettings,
                    alignmentStrategy = alignmentStrategy
                )
                blocks.add(block)
            }

            currentNode = currentNode.treeNext
        }

        return blocks
    }

    private fun isNewBlockToBeMade(currentNode: ASTNode) = currentNode.elementType != TokenType.WHITE_SPACE
        && currentNode.elementType != AclTypes.CRLF

}
