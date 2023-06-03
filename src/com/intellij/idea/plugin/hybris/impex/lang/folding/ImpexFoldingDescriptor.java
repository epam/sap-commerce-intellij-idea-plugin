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

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ImpexFoldingDescriptor extends FoldingDescriptor {

    private final String placeholder;

    public ImpexFoldingDescriptor(
        @NotNull final PsiElement psiElement,
        final int startOffset, final int endOffset,
        @NotNull final FoldingGroup group,
        Function<PsiElement, String> placeholderFunction
    ) {
        super(psiElement.getNode(), new TextRange(startOffset, endOffset), group);

        placeholder = placeholderFunction.apply(psiElement);
    }

    @Nullable
    @Override
    public String getPlaceholderText() {
        return placeholder;
    }
}