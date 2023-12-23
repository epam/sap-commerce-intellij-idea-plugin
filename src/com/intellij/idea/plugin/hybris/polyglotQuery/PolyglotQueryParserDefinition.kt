/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.polyglotQuery

import com.intellij.idea.plugin.hybris.polyglotQuery.file.PolyglotQueryFile
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class PolyglotQueryParserDefinition : ParserDefinition {

    private val fileNodeType = IFileElementType(PolyglotQueryLanguage.instance)

    override fun createLexer(project: Project) = PolyglotQueryLexer()
    override fun createParser(project: Project) = PolyglotQueryParser()
    override fun createElement(node: ASTNode): PsiElement = PolyglotQueryTypes.Factory.createElement(node)
    override fun createFile(viewProvider: FileViewProvider) = PolyglotQueryFile(viewProvider)

    override fun getFileNodeType(): IFileElementType = fileNodeType
    override fun getWhitespaceTokens(): TokenSet = TokenSet.WHITE_SPACE
    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY
    override fun getCommentTokens() = TokenSet.create(
        PolyglotQueryTypes.COMMENT,
        PolyglotQueryTypes.LINE_COMMENT
    )

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode) = ParserDefinition.SpaceRequirements.MAY

}