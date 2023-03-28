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

package com.intellij.idea.plugin.hybris.impex.assistance;

import com.intellij.codeInsight.folding.impl.FoldingUtil;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler;
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultImpexHeaderNameHighlighterService
    extends AbstractImpexHighlighterService
    implements ImpexHeaderNameHighlighterService {

    protected final Map<Editor, PsiElement> highlightedBlocks = new ConcurrentHashMap<>();

    @Override
    @Contract
    public void highlight(@NotNull final Editor editor) {
        highlightHeaderOfValueUnderCaret(editor);
    }

    @Contract
    protected void highlightHeaderOfValueUnderCaret(@NotNull final Editor editor) {
        final PsiElement header = ImpexPsiUtils.getHeaderOfValueGroupUnderCaret(editor);

        if (null == header) {
            this.clearHighlightedArea(editor);
        } else {
            this.highlightArea(editor, header);
        }
    }

    @Contract
    protected void highlightArea(
        @NotNull final Editor editor,
        @NotNull final PsiElement impexFullHeaderParameter
    ) {
        Validate.notNull(impexFullHeaderParameter);

        if (isAlreadyHighlighted(editor, impexFullHeaderParameter)) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            final PsiElement currentHighlightedElement = highlightedBlocks.remove(editor);
            if (null != currentHighlightedElement) {
                modifyHighlightedArea(editor, currentHighlightedElement, true);
            }

            highlightedBlocks.put(editor, impexFullHeaderParameter);
            modifyHighlightedArea(editor, impexFullHeaderParameter, false);
        });
    }

    @Contract
    protected void clearHighlightedArea(@NotNull final Editor editor) {
        if (!highlightedBlocks.isEmpty()) {
            final PsiElement impexFullHeaderParameter = highlightedBlocks.remove(editor);

            if (null != impexFullHeaderParameter) {
                ApplicationManager.getApplication().invokeLater(() ->
                    modifyHighlightedArea(editor, impexFullHeaderParameter, true)
                );
            }
        }
    }

    @Contract(pure = true)
    protected boolean isAlreadyHighlighted(
        @NotNull final Editor editor,
        @Nullable final PsiElement impexFullHeaderParameter
    ) {
        Validate.notNull(editor);

        return this.highlightedBlocks.get(editor) == impexFullHeaderParameter;
    }

    @Contract
    protected void modifyHighlightedArea(
        @NotNull final Editor editor,
        @NotNull final PsiElement impexFullHeaderParameter,
        final boolean clear
    ) {
        Validate.notNull(editor);
        Validate.notNull(impexFullHeaderParameter);

        if (null == editor.getProject()) {
            return;
        }

        if (editor.getProject().isDisposed()) {
            return;
        }

        this.removeInvalidRangeHighlighters(editor);

        // This list must be modifiable
        // https://hybris-integration.atlassian.net/browse/IIP-11
        final List<TextRange> ranges = new ArrayList<TextRange>();
        if (!FoldingUtil.isTextRangeFolded(editor, impexFullHeaderParameter.getTextRange())) {
            ranges.add(impexFullHeaderParameter.getTextRange());

            HighlightUsagesHandler.highlightRanges(
                HighlightManager.getInstance(editor.getProject()),
                editor,
                EditorColors.SEARCH_RESULT_ATTRIBUTES,
                clear,
                ranges
            );
        }
    }

    @Override
    @Contract
    public void releaseEditorData(@NotNull final Editor editor) {
        Validate.notNull(editor);

        this.highlightedBlocks.remove(editor);
    }
}