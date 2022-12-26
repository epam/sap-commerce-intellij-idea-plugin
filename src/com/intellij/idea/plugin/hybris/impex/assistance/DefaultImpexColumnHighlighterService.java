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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.intellij.codeInsight.folding.impl.FoldingUtil;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler;
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage;
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class DefaultImpexColumnHighlighterService
    extends AbstractImpexHighlighterService
    implements ImpexColumnHighlighterService {

    private final Cache<Editor, List<PsiElement>> cache =
        Caffeine.newBuilder()
                .maximumWeight(10_000)
                .weigher((Editor key, List<PsiElement> value) -> value.size())
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .build();

    @Override
    @Contract
    public void highlight(@NotNull final Editor editor) {
        Validate.notNull(editor);

        final Project project = editor.getProject();

        if (null == project) {
            return;
        }

        if (project.isDisposed()) {
            return;
        }

        final Language languageInEditor = PsiUtilBase.getLanguageInEditor(editor, project);

        if (languageInEditor instanceof ImpexLanguage) {
            this.highlightColumnOfValueUnderCaret(editor);
        }
    }

    @Contract
    protected void highlightColumnOfValueUnderCaret(@NotNull final Editor editor) {
        Validate.notNull(editor);

        final List<PsiElement> columns = ImpexPsiUtils.getColumnOfHeaderUnderCaret(editor);

        if (null == columns || columns.isEmpty()) {
            clearHighlightedArea(editor);
        } else {
            highlightArea(editor, columns);
        }
    }

    @Contract
    protected void highlightArea(
        @NotNull final Editor editor,
        @NotNull final List<PsiElement> column
    ) {
        Validate.notNull(editor);
        Validate.notNull(column);

        if (isAlreadyHighlighted(editor, column)) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            final List<PsiElement> currentHighlightedElement = cache.getIfPresent(editor);
            cache.invalidate(editor);
            if (null != currentHighlightedElement) {
                modifyHighlightedArea(editor, currentHighlightedElement, true);
            }

            cache.put(editor, column);
            modifyHighlightedArea(editor, column, false);
        });
    }

    @Contract
    protected void clearHighlightedArea(@NotNull final Editor editor) {
        Validate.notNull(editor);

        if (!cache.asMap().isEmpty()) {
            final List<PsiElement> column = cache.getIfPresent(editor);
            cache.invalidate(editor);
            if (null != column) {
                ApplicationManager.getApplication().invokeLater(() -> modifyHighlightedArea(editor, column, true));
            }
        }
    }

    @Contract
    protected boolean isAlreadyHighlighted(
        @NotNull final Editor editor,
        @Nullable final List<PsiElement> column
    ) {
        Validate.notNull(editor);

        return Objects.equals(cache.getIfPresent(editor), column);
    }

    @Contract
    protected void modifyHighlightedArea(
        @NotNull final Editor editor,
        @NotNull final List<PsiElement> column,
        final boolean clear
    ) {
        Validate.notNull(editor);
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
        Validate.notNull(editor);

        cache.invalidate(editor);
    }

}