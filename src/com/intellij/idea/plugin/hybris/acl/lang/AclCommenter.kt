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
package com.intellij.idea.plugin.hybris.acl.lang

import com.intellij.codeInsight.generation.CommenterDataHolder
import com.intellij.codeInsight.generation.SelfManagingCommenter
import com.intellij.idea.plugin.hybris.acl.psi.AclTypes
import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.util.text.CharArrayUtil

class AclCommenter : CodeDocumentationAwareCommenter, SelfManagingCommenter<CommenterDataHolder?> {

    override fun getLineCommentTokenType(): IElementType? = AclTypes.LINE_COMMENT

    override fun getLineCommentPrefix(): String = HASH_COMMENT_PREFIX
    override fun getCommentPrefix(line: Int, document: Document, data: CommenterDataHolder): String? = HASH_COMMENT_PREFIX
    override fun getBlockCommentPrefix(): String? = null
    override fun getBlockCommentSuffix(): String? = null
    override fun getCommentedBlockCommentPrefix(): String? = null
    override fun getCommentedBlockCommentSuffix(): String? = null

    override fun createLineCommentingState(startLine: Int, endLine: Int, document: Document, file: PsiFile): CommenterDataHolder? = null
    override fun createBlockCommentingState(selectionStart: Int, selectionEnd: Int, document: Document, file: PsiFile): CommenterDataHolder? = null

    override fun commentLine(line: Int, offset: Int, document: Document, data: CommenterDataHolder) = document
        .insertString(offset, HASH_COMMENT_PREFIX)
    override fun uncommentLine(line: Int, offset: Int, document: Document, data: CommenterDataHolder) = document
        .deleteString(offset, offset + HASH_COMMENT_PREFIX.length)

    override fun isLineCommented(line: Int, offset: Int, document: Document, data: CommenterDataHolder): Boolean = CharArrayUtil
        .regionMatches(document.charsSequence, offset, HASH_COMMENT_PREFIX)

    override fun getBlockCommentRange(selectionStart: Int, selectionEnd: Int, document: Document, data: CommenterDataHolder): TextRange? = throw UnsupportedOperationException()
    override fun getBlockCommentPrefix(selectionStart: Int, document: Document, data: CommenterDataHolder): String? = getBlockCommentPrefix()
    override fun getBlockCommentSuffix(selectionEnd: Int, document: Document, data: CommenterDataHolder): String? = getBlockCommentSuffix()
    override fun uncommentBlockComment(startOffset: Int, endOffset: Int, document: Document?, data: CommenterDataHolder?) = throw UnsupportedOperationException()
    override fun insertBlockComment(startOffset: Int, endOffset: Int, document: Document?, data: CommenterDataHolder?): TextRange = throw UnsupportedOperationException()

    override fun getBlockCommentTokenType(): IElementType? = null
    override fun getDocumentationCommentTokenType(): IElementType? = null
    override fun getDocumentationCommentPrefix(): String? = null
    override fun getDocumentationCommentLinePrefix(): String? = null
    override fun getDocumentationCommentSuffix(): String? = null
    override fun isDocumentationComment(element: PsiComment?): Boolean = false

    companion object {
        private const val HASH_COMMENT_PREFIX: String = "#"
    }
}