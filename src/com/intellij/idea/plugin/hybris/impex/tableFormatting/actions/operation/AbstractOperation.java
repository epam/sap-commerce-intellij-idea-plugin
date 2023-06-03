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

package com.intellij.idea.plugin.hybris.impex.tableFormatting.actions.operation;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexRootMacroUsage;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueLine;
import com.intellij.idea.plugin.hybris.impex.tableFormatting.ImpexTableEditor;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.findSiblingByPredicate;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.getNextSiblingOfAnyType;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isHeaderLine;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isImpexValueLine;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isLastElement;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isLineBreak;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isUserRightsMacros;
import static com.intellij.psi.util.PsiTreeUtil.getNextSiblingOfType;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static com.intellij.psi.util.PsiTreeUtil.getPrevSiblingOfType;
import static com.intellij.psi.util.PsiUtilBase.getElementAtCaret;

public abstract class AbstractOperation implements Runnable {

    protected final ImpexTableEditor editor;

    AbstractOperation(final ImpexTableEditor editor) {
        this.editor = editor;
    }

    @Override
    final public void run() {
        perform();
    }

    protected abstract void perform();

    @Nullable
    final Pair<PsiElement, PsiElement> getSelectedTable(final @NotNull ImpexTableEditor editor) {
        final PsiElement elementAtCaret = getElementAtCaret(editor.getIdeaEditor());


        final PsiElement valueLineAt;
        if (elementAtCaret != null) {
            if (getNextSiblingOfType(elementAtCaret, ImpexValueLine.class) != null) {
                valueLineAt = elementAtCaret.getNextSibling();
            } else if (getPrevSiblingOfType(elementAtCaret, ImpexValueLine.class) != null) {
                valueLineAt = elementAtCaret.getPrevSibling();
            } else {
                valueLineAt = getParentOfType(
                    elementAtCaret,
                    ImpexValueLine.class
                );

            }
        } else {
            return null;
        }

        if (valueLineAt == null) {
            return null;
        }

        final PsiElement headerLine = scanFirstLine(elementAtCaret, valueLineAt);

        final PsiElement lastValueLine = scanLastLine(valueLineAt, headerLine);

        return Pair.create(headerLine, lastValueLine);
    }

    @Nullable
    private PsiElement scanFirstLine(
        @NotNull final PsiElement elementAtCaret,
        @NotNull final PsiElement valueLineAt
    ) {
        final PsiElement headerLine;

        if (isHeaderLine(elementAtCaret)) {
            headerLine = elementAtCaret;
        } else if (isUserRightsMacros(elementAtCaret)) {
            headerLine = elementAtCaret;
        } else if (getParentOfType(elementAtCaret, ImpexRootMacroUsage.class) != null &&
                   isUserRightsMacros(getParentOfType(elementAtCaret, ImpexRootMacroUsage.class))) {
            headerLine = getParentOfType(elementAtCaret, ImpexRootMacroUsage.class);
        } else if (getParentOfType(elementAtCaret, ImpexHeaderLine.class) != null) {
            headerLine = getParentOfType(elementAtCaret, ImpexHeaderLine.class);
        } else {
            if (isHeaderLine(getPrevSiblingOfType(valueLineAt, ImpexHeaderLine.class))) {
                headerLine = getPrevSiblingOfType(
                    valueLineAt,
                    ImpexHeaderLine.class
                );
            } else if (isUserRightsMacros(getPrevSiblingOfType(valueLineAt, ImpexRootMacroUsage.class))) {
                headerLine = getPrevSiblingOfType(
                    valueLineAt,
                    ImpexRootMacroUsage.class
                );
            } else {
                return null;
            }
        }
        return headerLine;
    }

    @Nullable
    private PsiElement scanLastLine(final PsiElement valueLineAt, final PsiElement headerLine) {
        final PsiElement secondHeaderLine = getNextSiblingOfAnyType(
            valueLineAt,
            ImpexRootMacroUsage.class,
            ImpexHeaderLine.class
        );

        if (secondHeaderLine == null) {
            PsiElement lastValueLine = valueLineAt != null ? valueLineAt.getNextSibling() : headerLine.getNextSibling();
            final PsiElement nextSiblingOfAnyType = getNextSiblingOfAnyType(
                lastValueLine,
                ImpexHeaderLine.class,
                ImpexRootMacroUsage.class
            );

            if (nextSiblingOfAnyType != null) {
                lastValueLine = nextSiblingOfAnyType.getPrevSibling();
            } else {
                lastValueLine = findSiblingByPredicate(
                    lastValueLine,
                    (sibling) -> (isImpexValueLine(sibling) || isLineBreak(sibling)) && isLastElement(sibling)
                );
            }

            if (lastValueLine == null) {
                lastValueLine = valueLineAt;
            }

            return lastValueLine;
        }

        if (secondHeaderLine instanceof ImpexRootMacroUsage && secondHeaderLine.getText().startsWith("$END")) {
            return secondHeaderLine;
        }

        return getPrevSiblingOfType(
            secondHeaderLine,
            ImpexValueLine.class
        );
    }

}
