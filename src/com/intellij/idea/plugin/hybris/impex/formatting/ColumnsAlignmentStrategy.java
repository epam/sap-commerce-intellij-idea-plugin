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

package com.intellij.idea.plugin.hybris.impex.formatting;

import com.intellij.formatting.Alignment;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRightsValueGroup;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueGroup;
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12:55 01 January 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class ColumnsAlignmentStrategy implements AlignmentStrategy {

    protected final List<Alignment> alignments = new ArrayList<>();
    protected int columnNumber = 0;

    public ColumnsAlignmentStrategy() {
    }

    @Override
    public Alignment getAlignment(@NotNull final ASTNode currentNode) {
        Validate.notNull(currentNode);

        final Alignment alignment;

        if (isNewColumn(currentNode)) {
            if (columnNumber >= alignments.size()) {
                alignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT);
                alignments.add(alignment);
            } else {
                alignment = alignments.get(columnNumber);
            }
            columnNumber++;
        } else {
            alignment = Alignment.createAlignment();
        }

        return alignment;
    }

    @Override
    public void processNode(@NotNull final ASTNode currentNode) {
        Validate.notNull(currentNode);

        if (isStartOfTheFile(currentNode)) {
            columnNumber = 0;
            alignments.clear();
        } else {
            if (isNewLine(currentNode)) {
                columnNumber = 0;
            }

            if (isHeaderLine(currentNode)) {
                alignments.clear();
            }

            if (ImpexPsiUtils.isUserRightsMacros(currentNode.getPsi())) {
                alignments.clear();
            }
        }
    }

    @Contract(pure = true)
    protected boolean isStartOfTheFile(@Nullable final ASTNode currentNode) {
        return null != currentNode && currentNode.getPsi() instanceof ImpexFile;
    }

    @Contract(pure = true)
    protected boolean isNewLine(@Nullable final ASTNode currentNode) {
        return null != currentNode
            && (ImpexTypes.VALUE_GROUP == currentNode.getElementType()
            || ImpexTypes.USER_RIGHTS_VALUE_GROUP == currentNode.getElementType())
            && isStartOfValueLine(currentNode);
    }

    @Contract(pure = true)
    protected boolean isStartOfValueLine(@Nullable final ASTNode currentNode) {
        if (null == currentNode) {
            return false;
        }

        final PsiElement child = PsiTreeUtil.findChildOfAnyType(
            currentNode.getTreeParent().getPsi(),
            ImpexValueGroup.class, ImpexUserRightsValueGroup.class
        );

        return child == currentNode.getPsi();
    }

    @Contract(pure = true)
    protected boolean isNewColumn(@Nullable final ASTNode currentNode) {
        return null != currentNode &&
            (ImpexTypes.VALUE_GROUP == currentNode.getElementType()
                || ImpexTypes.USER_RIGHTS_VALUE_GROUP == currentNode.getElementType());
    }

    @Contract(pure = true)
    protected boolean isHeaderLine(@Nullable final ASTNode currentNode) {
        return null != currentNode &&
            (ImpexTypes.HEADER_LINE == currentNode.getElementType()
                || ImpexTypes.USER_RIGHTS_HEADER_LINE == currentNode.getElementType());
    }
}
