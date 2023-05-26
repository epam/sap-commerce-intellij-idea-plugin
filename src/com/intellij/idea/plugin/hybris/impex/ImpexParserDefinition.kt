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
package com.intellij.idea.plugin.hybris.impex

import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ParserDefinition
import com.intellij.lexer.FlexAdapter
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class ImpexParserDefinition : ParserDefinition {

    override fun createLexer(project: Project) = FlexAdapter(ImpexLexer(null))
    override fun createParser(project: Project) = ImpexParser()
    override fun createElement(node: ASTNode): PsiElement = ImpexTypes.Factory.createElement(node)
    override fun createFile(viewProvider: FileViewProvider) = ImpexFile(viewProvider)

    override fun getFileNodeType() = FILE_NODE_TYPE
    override fun getWhitespaceTokens() = WHITE_SPACES
    override fun getCommentTokens() = COMMENTS
    override fun getStringLiteralElements() = STRING_LITERALS
    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode) = ParserDefinition.SpaceRequirements.MAY

    companion object {
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(ImpexTypes.LINE_COMMENT)
        val STRING_LITERALS = TokenSet.create(
            ImpexTypes.SINGLE_STRING,
            ImpexTypes.DOUBLE_STRING,
            ImpexTypes.STRING
        )
        val FILE_NODE_TYPE = IFileElementType(Language.findInstance(ImpexLanguage::class.java))
    }
}
