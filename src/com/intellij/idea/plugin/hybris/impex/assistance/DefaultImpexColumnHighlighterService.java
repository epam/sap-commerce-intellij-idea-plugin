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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValue;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueGroup;
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class DefaultImpexColumnHighlighterService
    extends AbstractImpexHighlighterService
    implements ImpexColumnHighlighterService {

    private final static Key<List<SmartPsiElementPointer<ImpexValue>>> CACHE_KEY = Key.create("IMPEX_COLUMN_HIGHLIGHT_CACHE");

    @Override
    @Contract
    public void highlight(@NotNull final Editor editor) {
        highlightColumnOfValueUnderCaret(editor);
    }

    @Contract
    protected void highlightColumnOfValueUnderCaret(@NotNull final Editor editor) {
        final var headerParameter = ImpexPsiUtils.getFullHeaderParameterUnderCaret(editor);
        if (headerParameter == null) {
            clearHighlightedArea(editor);
            return;
        }

        final var columns = headerParameter.getValueGroups().stream()
            .map(ImpexValueGroup::getValue)
            .filter(Objects::nonNull)
            .toList();

        if (columns.isEmpty()) {
            clearHighlightedArea(editor);
        } else if (editor.getProject() != null) {
            final var pointerManager = SmartPointerManager.getInstance(editor.getProject());
            final var pointers = columns.stream()
                .map(pointerManager::createSmartPsiElementPointer)
                .toList();
            highlightArea(editor, pointers);
        }
    }

    @Contract
    protected void highlightArea(
        @NotNull final Editor editor,
        final List<SmartPsiElementPointer<ImpexValue>> column
    ) {
        Validate.notNull(column);

        if (isAlreadyHighlighted(editor, column)) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            final var currentHighlightedElement = editor.getUserData(CACHE_KEY);
            editor.putUserData(CACHE_KEY, null);
            if (null != currentHighlightedElement) {
                modifyHighlightedArea(editor, currentHighlightedElement, true);
            }

            editor.putUserData(CACHE_KEY, column);
            modifyHighlightedArea(editor, column, false);
        });
    }

    @Contract
    protected void clearHighlightedArea(@NotNull final Editor editor) {
        final var column = editor.getUserData(CACHE_KEY);
        if (column != null) {
            editor.putUserData(CACHE_KEY, null);
            ApplicationManager.getApplication().invokeLater(() -> modifyHighlightedArea(editor, column, true));
        }
    }

    @Contract
    protected boolean isAlreadyHighlighted(
        @NotNull final Editor editor,
        final List<SmartPsiElementPointer<ImpexValue>> column
    ) {
        return Objects.equals(editor.getUserData(CACHE_KEY), column);
    }

    @Contract
    protected void modifyHighlightedArea(
        @NotNull final Editor editor,
        final List<SmartPsiElementPointer<ImpexValue>> column,
        final boolean clear
    ) {
        Validate.notNull(column);

        if (null == editor.getProject()) {
            return;
        }

        if (editor.getProject().isDisposed()) {
            return;
        }

        this.removeInvalidRangeHighlighters(editor);

        // This list must be modifiable
        // https://hybris-integration.atlassian.net/browse/IIP-11
        final List<TextRange> ranges = column
            .stream()
            .filter(Objects::nonNull)
            .map(SmartPsiElementPointer::getElement)
            .filter(Objects::nonNull)
            .map(PsiElement::getTextRange)
            .filter(textRange -> !FoldingUtil.isTextRangeFolded(editor, textRange))
            // Do not use Collectors.toList() here because:
            // There are no guarantees on the type, mutability, serializability,
            // or thread-safety of the List returned; if more control over the
            // returned List is required, use toCollection(Supplier).
            .collect(Collectors.toCollection(ArrayList::new));

        if (CollectionUtils.isNotEmpty(ranges)) {
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
        editor.putUserData(CACHE_KEY, null);
    }

}