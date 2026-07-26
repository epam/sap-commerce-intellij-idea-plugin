/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sap.commerce.toolset.impex.psi.*;

import java.util.List;
import java.util.Objects;

@Deprecated(since = "Convert to kotlin and move to psi package")
public final class ImpExPsiUtils {

    private ImpExPsiUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isMacroNameDeclaration(@Nullable final PsiElement element) {
        return element instanceof ImpExMacroNameDec || Objects.equals(ImpExCommonPsiUtils.getNullSafeElementType(element), ImpExTypes.MACRO_NAME_DECLARATION);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isMacroUsage(@Nullable final PsiElement element) {
        return element instanceof ImpExMacroUsageDec || Objects.equals(ImpExCommonPsiUtils.getNullSafeElementType(element), ImpExTypes.MACRO_USAGE);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isImpExValueLine(@Nullable final PsiElement psiElement) {
        return psiElement instanceof ImpExValueLine;
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isWhiteSpace(@Nullable final PsiElement psiElement) {
        return psiElement instanceof PsiWhiteSpace;
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isLineBreak(@Nullable final PsiElement psiElement) {
        return ImpExTypes.CRLF == ImpExCommonPsiUtils.getNullSafeElementType(psiElement);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isFieldValueSeparator(@Nullable final PsiElement psiElement) {
        return ImpExTypes.FIELD_VALUE_SEPARATOR == ImpExCommonPsiUtils.getNullSafeElementType(psiElement);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isUserRightsMacros(@Nullable final PsiElement psiElement) {
        return psiElement != null && Objects.equals(
            ImpExTypes.ROOT_MACRO_USAGE,
            ImpExCommonPsiUtils.getNullSafeElementType(psiElement)
        ) && (psiElement.getText().contains("$START_USERRIGHTS") || psiElement.getText().contains("$END_USERRIGHTS"));

    }


    public static boolean prevElementIsUserRightsMacros(@NotNull final PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, ImpExUserRights.class) != null;
    }


    @Nullable
    public static PsiElement getHeaderOfValueGroupUnderCaret(@NotNull final Editor editor) {
        final var psiElementUnderCaret = PsiUtilBase.getElementAtCaret(editor);
        if (null == psiElementUnderCaret) return null;

        final var valueGroup = getClosestSelectedValueGroupFromTheSameLine(psiElementUnderCaret);

        if (valueGroup == null) return null;

        return getHeaderForValueGroup(valueGroup);
    }

    @Nullable
    public static ImpExFullHeaderParameter getFullHeaderParameterUnderCaret(@NotNull final Editor editor) {
        final var psiElementUnderCaret = PsiUtilBase.getElementAtCaret(editor);
        if (psiElementUnderCaret == null) return null;

        return PsiTreeUtil.getParentOfType(psiElementUnderCaret, ImpExFullHeaderParameter.class);
    }

    @Nullable
    @Contract(pure = true)
    public static ImpExValueGroup getClosestSelectedValueGroupFromTheSameLine(@Nullable final PsiElement psiElementUnderCaret) {
        if (null == psiElementUnderCaret) {
            return null;
        }

        if (psiElementUnderCaret instanceof ImpExValueGroup) {
            return (ImpExValueGroup) psiElementUnderCaret;
        } else if (isFieldValueSeparator(psiElementUnderCaret)) {

            final ImpExValueGroup valueGroup = PsiTreeUtil.getParentOfType(psiElementUnderCaret, ImpExValueGroup.class);
            if (null != valueGroup) {
                return PsiTreeUtil.getPrevSiblingOfType(valueGroup, ImpExValueGroup.class);
            }

        } else if (isWhiteSpace(psiElementUnderCaret)) {

            ImpExValueGroup valueGroup = PsiTreeUtil.getParentOfType(psiElementUnderCaret, ImpExValueGroup.class);

            if (null == valueGroup) {
                valueGroup = PsiTreeUtil.getPrevSiblingOfType(psiElementUnderCaret, ImpExValueGroup.class);
            }

            if (null == valueGroup) {
                valueGroup = skipAllExceptLineBreaksAndGetImpExValueGroup(psiElementUnderCaret);
            }

            return valueGroup;

        } else if (isLineBreak(psiElementUnderCaret)) {

            return skipAllExceptLineBreaksAndGetImpExValueGroup(psiElementUnderCaret);

        } else {
            return PsiTreeUtil.getParentOfType(psiElementUnderCaret, ImpExValueGroup.class);
        }

        return null;
    }

    @Nullable
    @Contract(pure = true)
    public static PsiElement getHeaderForValueGroup(@Nullable final ImpExValueGroup valueGroup) {
        if (null == valueGroup) return null;

        final var columnNumber = valueGroup.getColumnNumber();

        if (columnNumber < 0) return null;

        final var impexValueLine = valueGroup.getValueLine();
        if (impexValueLine == null) return null;

        if (prevElementIsUserRightsMacros(impexValueLine)) return null;

        final var impexHeaderLine = impexValueLine.getHeaderLine();
        if (impexHeaderLine == null) return null;

        final var header = impexHeaderLine.getFullHeaderParameter(columnNumber);

        return header != null
            ? header
            : getHeaderParametersSeparatorFromHeaderLineByNumber(columnNumber, impexHeaderLine);
    }

    @Nullable
    @Contract(pure = true)
    public static ImpExValueGroup skipAllExceptLineBreaksAndGetImpExValueGroup(
        @NotNull final PsiElement psiElement
    ) {
        if (isLineBreak(psiElement.getPrevSibling())) {
            return null;
        }

        PsiElement prevSibling = psiElement.getPrevSibling();
        while (!isImpExValueLine(prevSibling)) {
            if (null == prevSibling || isLineBreak(prevSibling)) {
                return null;
            }

            prevSibling = prevSibling.getPrevSibling();
        }

        if (!isImpExValueLine(prevSibling)) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(PsiTreeUtil.lastChild(prevSibling), ImpExValueGroup.class);
    }

    @Contract(pure = true)
    public static int getColumnNumber(@NotNull final ImpExValueGroup valueGroup) {
        final List<ImpExValueGroup> valueGroups = PsiTreeUtil.getChildrenOfTypeAsList(valueGroup.getValueLine(), ImpExValueGroup.class);

        int columnNumber = 0;

        for (ImpExValueGroup group : valueGroups) {
            if (group == valueGroup) {
                return columnNumber;
            }

            columnNumber++;
        }

        return -1;
    }

    @Contract(pure = true)
    public static int getColumnNumber(@NotNull final ImpExFullHeaderParameter element) {
        final List<ImpExFullHeaderParameter> groups = PsiTreeUtil.getChildrenOfTypeAsList(element.getHeaderLine(), ImpExFullHeaderParameter.class);

        int columnNumber = 0;

        for (ImpExFullHeaderParameter group : groups) {
            if (group == element) {
                return columnNumber;
            }

            columnNumber++;
        }

        return -1;
    }

    @Nullable
    @Contract(pure = true)
    public static PsiElement getHeaderParametersSeparatorFromHeaderLineByNumber(
        final int columnNumber,
        @NotNull final ImpExHeaderLine impexHeaderLine
    ) {
        Validate.isTrue(columnNumber >= 0);

        final List<PsiElement> parameterSeparators = ImpExCommonPsiUtils.findChildrenByIElementType(
            impexHeaderLine, ImpExTypes.PARAMETERS_SEPARATOR
        );

        if (columnNumber >= parameterSeparators.size()) {
            return null;
        }

        return parameterSeparators.get(columnNumber);
    }

}
