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
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.findSiblingBackward;

public final class CommonAstUtils {

    private CommonAstUtils() throws IllegalAccessException {
        throw new IllegalAccessException("Should never be accessed.");
    }

    @Contract(pure = true)
    public static IElementType getNullSafeElementType(@Nullable final ASTNode node) {
        return node == null ? null : node.getElementType();
    }

    @Contract(pure = true)
    public static ASTNode getPrevSibling(@Nullable final ASTNode node, final IElementType type) {
        if (node != null) {
            final PsiElement siblingBackward = findSiblingBackward(node.getPsi(), type, null);
            if (siblingBackward != null) {
                return siblingBackward.getNode();
            }
        }
        return null;
    }

}
