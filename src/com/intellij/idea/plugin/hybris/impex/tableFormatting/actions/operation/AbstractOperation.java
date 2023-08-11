/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2014-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        final var elementAtCaret = PsiUtilBase.getElementAtCaret(editor.getIdeaEditor());

        if (elementAtCaret == null) return null;

        PsiElement valueLineAt;
        if (PsiTreeUtil.getNextSiblingOfType(elementAtCaret, ImpexValueLine.class) != null) {
            valueLineAt = elementAtCaret.getNextSibling();
        } else if (PsiTreeUtil.getPrevSiblingOfType(elementAtCaret, ImpexValueLine.class) != null) {
            valueLineAt = elementAtCaret.getPrevSibling();
        } else {
            valueLineAt = PsiTreeUtil.getParentOfType(
                elementAtCaret,
                ImpexValueLine.class
            );
        }

        if (valueLineAt == null) {
            valueLineAt = PsiTreeUtil.getParentOfType(elementAtCaret, ImpexHeaderLine.class);
        }

        if (valueLineAt == null) return null;

        final var headerLine = scanFirstLine(elementAtCaret, valueLineAt);
        final var lastValueLine = scanLastLine(valueLineAt, headerLine);

        return Pair.create(headerLine, lastValueLine);
    }

    @Nullable
    private PsiElement scanFirstLine(
        @NotNull final PsiElement elementAtCaret,
        @NotNull final PsiElement valueLineAt
    ) {
        final PsiElement headerLine;

        if (ImpexPsiUtils.isHeaderLine(elementAtCaret)) {
            headerLine = elementAtCaret;
        } else if (ImpexPsiUtils.isUserRightsMacros(elementAtCaret)) {
            headerLine = elementAtCaret;
        } else if (PsiTreeUtil.getParentOfType(elementAtCaret, ImpexRootMacroUsage.class) != null &&
                   ImpexPsiUtils.isUserRightsMacros(PsiTreeUtil.getParentOfType(elementAtCaret, ImpexRootMacroUsage.class))) {
            headerLine = PsiTreeUtil.getParentOfType(elementAtCaret, ImpexRootMacroUsage.class);
        } else if (PsiTreeUtil.getParentOfType(elementAtCaret, ImpexHeaderLine.class) != null) {
            headerLine = PsiTreeUtil.getParentOfType(elementAtCaret, ImpexHeaderLine.class);
        } else {
            if (ImpexPsiUtils.isHeaderLine(PsiTreeUtil.getPrevSiblingOfType(valueLineAt, ImpexHeaderLine.class))) {
                headerLine = PsiTreeUtil.getPrevSiblingOfType(
                    valueLineAt,
                    ImpexHeaderLine.class
                );
            } else if (ImpexPsiUtils.isUserRightsMacros(PsiTreeUtil.getPrevSiblingOfType(valueLineAt, ImpexRootMacroUsage.class))) {
                headerLine = PsiTreeUtil.getPrevSiblingOfType(
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
        final PsiElement secondHeaderLine = ImpexPsiUtils.getNextSiblingOfAnyType(
            valueLineAt,
            ImpexRootMacroUsage.class,
            ImpexHeaderLine.class
        );

        if (secondHeaderLine == null) {
            PsiElement lastValueLine = valueLineAt != null ? valueLineAt.getNextSibling() : headerLine.getNextSibling();
            final PsiElement nextSiblingOfAnyType = ImpexPsiUtils.getNextSiblingOfAnyType(
                lastValueLine,
                ImpexHeaderLine.class,
                ImpexRootMacroUsage.class
            );

            if (nextSiblingOfAnyType != null) {
                lastValueLine = nextSiblingOfAnyType.getPrevSibling();
            } else {
                lastValueLine = ImpexPsiUtils.findSiblingByPredicate(
                    lastValueLine,
                    (sibling) -> (ImpexPsiUtils.isImpexValueLine(sibling) || ImpexPsiUtils.isLineBreak(sibling)) && ImpexPsiUtils.isLastElement(sibling)
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

        return PsiTreeUtil.getPrevSiblingOfType(
            secondHeaderLine,
            ImpexValueLine.class
        );
    }

}
