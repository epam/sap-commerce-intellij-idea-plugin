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

import com.intellij.idea.plugin.hybris.impex.constants.modifier.ImpexModifier;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeValue;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderMode;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexModifiers;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameters;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueLine;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.aroundIsValueLine;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isHeaderLine;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isLineBreak;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isUserRightsMacros;

public class ImpexFoldingLinesFilter implements PsiElementFilter {

    @Override
    public boolean isAccepted(final PsiElement element) {
        return null != element && (isFoldable(element) && isNotFoldableParent(element));
    }

    @Contract(pure = true)
    private boolean isFoldable(@Nullable final PsiElement element) {
        return null != element
               && this.isSupportedType(element)
               && !(isLineBreak(element) && isHeaderLine(element.getPrevSibling()))
               && !(isLineBreak(element) && isUserRightsMacros(element.getPrevSibling()))
               && !(isLineBreak(element) && element.getNextSibling() == null);
    }


    /**
     * Optimized method.
     *
     * @param element
     *
     * @return
     */
    @Contract(pure = true)
    private boolean isSupportedType(final @Nullable PsiElement element) {
        return !(element instanceof PsiFileSystemItem) &&
               !(element instanceof ImpexAnyAttributeName) &&
               !(element instanceof ImpexAnyAttributeValue) &&
               !(element instanceof ImpexAnyHeaderParameterName) &&
               !(element instanceof ImpexAttribute) &&
               !(element instanceof ImpexFullHeaderType) &&
               !(element instanceof ImpexFullHeaderParameter) &&
               !(element instanceof ImpexHeaderTypeName) &&
               !(element instanceof ImpexParameter) &&
               !(element instanceof ImpexParameters) &&
               !(element instanceof ImpexAnyHeaderMode) &&
               !(element instanceof ImpexModifier) &&
               !(element instanceof ImpexModifiers) &&
               PsiTreeUtil.getParentOfType(element, ImpexHeaderLine.class) == null &&
               (element instanceof ImpexValueLine || element instanceof ImpexHeaderLine || aroundIsValueLine(element));
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
