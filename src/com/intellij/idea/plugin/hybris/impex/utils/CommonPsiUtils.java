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

package com.intellij.idea.plugin.hybris.impex.utils;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.tree.IElementType;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.idea.plugin.hybris.common.JavaConstants.SETTER_PREFIX;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

public final class CommonPsiUtils {

    private CommonPsiUtils() throws IllegalAccessException {
        throw new IllegalAccessException("Should never be accessed.");
    }


    @Nullable
    @Contract("null, _ -> null")
    public static <T extends PsiElement> T getNextSiblingOfAnyType(
        @Nullable final PsiElement sibling,
        @NotNull final Class<?>... aClasses
    ) {
        if (sibling == null) {
            return null;
        }
        for (PsiElement child = sibling.getNextSibling(); child != null; child = child.getNextSibling()) {
            for (final Class<?> aClass : aClasses) {
                if (aClass.isInstance(child)) {
                    //noinspection unchecked
                    return (T) child;
                }
            }
        }
        return null;
    }


    @Nullable
    @Contract(pure = true)
    public static IElementType getNullSafeElementType(@Nullable final PsiElement element) {
        return element == null ? null : CommonAstUtils.getNullSafeElementType(element.getNode());
    }

    @Nullable
    @Contract(pure = true)
    public static PsiElement getNextNonWhitespaceElement(@NotNull final PsiElement element) {
        Validate.notNull(element);

        PsiElement nextSibling = element.getNextSibling();

        while (null != nextSibling && ImpexPsiUtils.isWhiteSpace(nextSibling)) {
            nextSibling = nextSibling.getNextSibling();
        }

        return nextSibling;
    }

    @NotNull
    @Contract(pure = true)
    public static List<PsiElement> findChildrenByIElementType(
        @NotNull final PsiElement element,
        @NotNull final IElementType elementType
    ) {
        Validate.notNull(element);
        Validate.notNull(elementType);

        List<PsiElement> result = Collections.emptyList();
        ASTNode child = element.getNode().getFirstChildNode();

        while (child != null) {
            if (elementType == child.getElementType()) {
                if (result.isEmpty()) {
                    result = new ArrayList<PsiElement>();
                }
                result.add(child.getPsi());
            }
            child = child.getTreeNext();
        }

        return result;
    }

    @Contract(pure = true)
    public static boolean isSetter(@NotNull final PsiMethod psiMethod) {
        Validate.notNull(psiMethod);

        return !isBlank(psiMethod.getName()) && startsWith(psiMethod.getName(), SETTER_PREFIX);
    }

}
