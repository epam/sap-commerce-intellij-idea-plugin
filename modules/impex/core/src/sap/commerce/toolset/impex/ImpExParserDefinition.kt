/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.impex

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lexer.FlexAdapter
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import sap.commerce.toolset.impex.psi.ImpExFile
import sap.commerce.toolset.impex.psi.ImpExTypes

class ImpExParserDefinition : ParserDefinition {

    override fun createLexer(project: Project) = FlexAdapter(ImpExLexer(null))
    override fun createParser(project: Project) = ImpExParser()
    override fun createElement(node: ASTNode): PsiElement = ImpExTypes.Factory.createElement(node)
    override fun createFile(viewProvider: FileViewProvider) = ImpExFile(viewProvider)

    override fun getFileNodeType() = ImpExConstants.FILE_NODE_TYPE
    override fun getWhitespaceTokens(): TokenSet = TokenSet.WHITE_SPACE
    override fun getCommentTokens() = TokenSet.create(ImpExTypes.LINE_COMMENT)
    override fun getStringLiteralElements() = TokenSet.create(
        ImpExTypes.SINGLE_STRING,
        ImpExTypes.DOUBLE_STRING,
        ImpExTypes.STRING
    )

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode) = ParserDefinition.SpaceRequirements.MAY

}
