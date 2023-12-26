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

package com.intellij.idea.plugin.hybris.impex.lang;

import com.intellij.codeInsight.generation.CommenterDataHolder;
import com.intellij.codeInsight.generation.SelfManagingCommenter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImpexCommenter implements CodeDocumentationAwareCommenter, SelfManagingCommenter<CommenterDataHolder> {

    public static final String HASH_COMMENT_PREFIX = "#";

    @Override
    public String getLineCommentPrefix() {
        return HASH_COMMENT_PREFIX;
    }

    @Override
    public String getBlockCommentPrefix() {
        return null;
    }

    @Override
    public String getBlockCommentSuffix() {
        return null;
    }

    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Nullable
    @Override
    public CommenterDataHolder createLineCommentingState(final int startLine, final int endLine, @NotNull final Document document, @NotNull final PsiFile file) {
        return null;
    }

    @Nullable
    @Override
    public CommenterDataHolder createBlockCommentingState(
        final int selectionStart,
        final int selectionEnd,
        @NotNull final Document document,
        @NotNull final PsiFile file) {
        return null;
    }

    @Override
    public void commentLine(final int line, final int offset, @NotNull final Document document, @NotNull final CommenterDataHolder data) {
        document.insertString(offset, HASH_COMMENT_PREFIX);
    }

    @Override
    public void uncommentLine(final int line, final int offset, @NotNull final Document document, @NotNull final CommenterDataHolder data) {
        document.deleteString(offset, offset + HASH_COMMENT_PREFIX.length());
    }

    @Override
    public boolean isLineCommented(final int line, final int offset, @NotNull final Document document, @NotNull final CommenterDataHolder data) {
        return CharArrayUtil.regionMatches(document.getCharsSequence(), offset, HASH_COMMENT_PREFIX);
    }

    @Nullable
    @Override
    public String getCommentPrefix(final int line, @NotNull final Document document, @NotNull final CommenterDataHolder data) {
        return HASH_COMMENT_PREFIX;
    }

    @Nullable
    @Override
    public TextRange getBlockCommentRange(
        final int selectionStart,
        final int selectionEnd,
        @NotNull final Document document,
        @NotNull final CommenterDataHolder data) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public String getBlockCommentPrefix(final int selectionStart, @NotNull final Document document, @NotNull final CommenterDataHolder data) {
        return getBlockCommentPrefix();
    }

    @Nullable
    @Override
    public String getBlockCommentSuffix(final int selectionEnd, @NotNull final Document document, @NotNull final CommenterDataHolder data) {
        return getBlockCommentSuffix();
    }

    @Override
    public void uncommentBlockComment(final int startOffset, final int endOffset, final Document document, final CommenterDataHolder data) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public TextRange insertBlockComment(final int startOffset, final int endOffset, final Document document, final CommenterDataHolder data) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public IElementType getLineCommentTokenType() {
        return ImpexTypes.LINE_COMMENT;
    }

    @Nullable
    @Override
    public IElementType getBlockCommentTokenType() {
        return null;
    }

    @Nullable
    @Override
    public IElementType getDocumentationCommentTokenType() {
        return null;
    }

    @Nullable
    @Override
    public String getDocumentationCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getDocumentationCommentLinePrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getDocumentationCommentSuffix() {
        return null;
    }

    @Override
    public boolean isDocumentationComment(final PsiComment element) {
        return false;
    }
}