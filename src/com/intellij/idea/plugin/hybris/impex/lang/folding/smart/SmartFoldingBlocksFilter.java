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

package com.intellij.idea.plugin.hybris.impex.lang.folding.smart;

import com.intellij.idea.plugin.hybris.impex.lang.folding.ImpexFoldingPlaceholderBuilderFactory;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameters;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiElementFilter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isLineBreak;

/**
 * Created 22:40 01 January 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class SmartFoldingBlocksFilter implements PsiElementFilter {

    @Override
    public boolean isAccepted(@Nullable final PsiElement eachElement) {
        return null != eachElement && (isFoldable(eachElement) && isNotFoldableParent(eachElement));
    }

    @Contract(pure = true)
    private boolean isFoldable(@Nullable final PsiElement element) {
        return null != element
               && this.isSupportedType(element)
               && (isLineBreak(element) || this.isNotBlankPlaceholder(element));
    }

    @Contract(pure = true)
    private boolean isNotBlankPlaceholder(final @Nullable PsiElement element) {
        return (null != element) && !StringUtils.isBlank(
            ImpexFoldingPlaceholderBuilderFactory.getPlaceholderBuilder(element.getProject()).getPlaceholder(element)
        );
    }

    @Contract(pure = true)
    private boolean isSupportedType(final @Nullable PsiElement element) {
        return element instanceof ImpexAttribute
               || element instanceof ImpexParameters
               || isLineBreak(element);
    }

    @Contract(pure = true)
    private boolean isNotFoldableParent(@Nullable final PsiElement element) {
        if (null == element) {
            return false;
        }

        PsiElement parent = element.getParent();
        while (null != parent) {
            if (isFoldable(parent)) {
                return false;
            }

            parent = parent.getParent();
        }

        return true;
    }
}