/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.impex.lang.folding;

import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SyntaxTraverser;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isLineBreak;

public class ImpexFoldingBuilder extends FoldingBuilderEx {

    private static final String GROUP_NAME = "ImpEx";

    private static final FoldingDescriptor[] EMPTY_ARRAY = new FoldingDescriptor[0];

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(
        @NotNull final PsiElement root,
        @NotNull final Document document,
        final boolean quick
    ) {
        if (this.isFoldingDisabled(root.getProject())) {
            return EMPTY_ARRAY;
        }

        Validate.notNull(root);
        Validate.notNull(document);

        final Collection<PsiElement> psiElements = this.findFoldingBlocksAndLineBreaks(root);

        FoldingGroup currentLineGroup = FoldingGroup.newGroup(GROUP_NAME);

        /* Avoid spawning a lot of unnecessary objects for each line break. */
        boolean groupIsNotFresh = false;

        final List<FoldingDescriptor> descriptors = new ArrayList<>();
        for (final PsiElement psiElement : psiElements) {

            if (isLineBreak(psiElement)) {
                if (groupIsNotFresh) {
                    currentLineGroup = FoldingGroup.newGroup(GROUP_NAME);
                    groupIsNotFresh = false;
                }
            } else {
                descriptors.add(new FoldingDescriptor(psiElement.getNode(), psiElement.getTextRange(), currentLineGroup));
                groupIsNotFresh = true;
            }
        }

        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }


    @Contract(pure = true)
    protected boolean isFoldingDisabled(final @NotNull Project project) {
        return !HybrisProjectSettingsComponent.getInstance(project).getState()
            .getImpexSettings()
            .getFolding()
            .getEnabled();
    }

    @NotNull
    @Contract(pure = true)
    protected Collection<PsiElement> findFoldingBlocksAndLineBreaks(@Nullable final PsiElement root) {
        if (root == null) {
            return Collections.emptyList();
        }

        final var filter = PsiElementFilterFactory.getPsiElementFilter(root.getProject());
        return SyntaxTraverser.psiTraverser(root)
                              .filter(filter::isAccepted)
                              .toList();
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull final ASTNode node) {
        Validate.notNull(node);

        final PsiElement psi = node.getPsi();
        String text = ImpexFoldingPlaceholderBuilderFactory.getPlaceholderBuilder(psi.getProject()).getPlaceholder(psi);
        String resolvedMacro = text;
        if (text.startsWith("$")) {
            final Map<String, ImpexMacroDescriptor> cache = ImpexMacroUtils.getFileCache(psi.getContainingFile()).getValue();
            final ImpexMacroDescriptor descriptor = cache.get(text);
            if (descriptor != null) {
                resolvedMacro = descriptor.resolvedValue();
            }
        }
        if (resolvedMacro.length() <= text.length()) {
            return resolvedMacro;
        }
        return text;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull final ASTNode node) {
        return true;
    }
}
