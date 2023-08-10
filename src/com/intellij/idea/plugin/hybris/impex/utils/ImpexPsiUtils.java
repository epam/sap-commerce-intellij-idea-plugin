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

import com.intellij.idea.plugin.hybris.impex.psi.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class ImpexPsiUtils {

    private ImpexPsiUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isMacroNameDeclaration(@Nullable final PsiElement element) {
        return element instanceof ImpexMacroNameDec || Objects.equals(CommonPsiUtils.getNullSafeElementType(element), ImpexTypes.MACRO_NAME_DECLARATION);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isMacroUsage(@Nullable final PsiElement element) {
        return element instanceof ImpexMacroUsageDec || Objects.equals(CommonPsiUtils.getNullSafeElementType(element), ImpexTypes.MACRO_USAGE);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isImpexValueLine(@Nullable final PsiElement psiElement) {
        return psiElement instanceof ImpexValueLine;
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isWhiteSpace(@Nullable final PsiElement psiElement) {
        return psiElement instanceof PsiWhiteSpace;
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isLineBreak(@Nullable final PsiElement psiElement) {
        return ImpexTypes.CRLF == CommonPsiUtils.getNullSafeElementType(psiElement);
    }

    @Contract(pure = true)
    public static boolean isLastElement(final PsiElement element) {
        return element.getNextSibling() == null;
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isFieldValueSeparator(@Nullable final PsiElement psiElement) {
        return ImpexTypes.FIELD_VALUE_SEPARATOR == CommonPsiUtils.getNullSafeElementType(psiElement);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isParameterSeparator(@Nullable final PsiElement psiElement) {
        return ImpexTypes.PARAMETERS_SEPARATOR == CommonPsiUtils.getNullSafeElementType(psiElement);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isImpexFullHeaderParameter(@Nullable final PsiElement psiElement) {
        return ImpexTypes.FULL_HEADER_PARAMETER == CommonPsiUtils.getNullSafeElementType(psiElement);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isHeaderLine(@Nullable final PsiElement psiElement) {
        return Objects.equals(ImpexTypes.HEADER_LINE, CommonPsiUtils.getNullSafeElementType(psiElement));
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isUserRightsMacros(@Nullable final PsiElement psiElement) {
        return psiElement != null && Objects.equals(
            ImpexTypes.ROOT_MACRO_USAGE,
            CommonPsiUtils.getNullSafeElementType(psiElement)
        ) && (psiElement.getText().contains("$START_USERRIGHTS") || psiElement.getText().contains("$END_USERRIGHTS"));

    }


    @Nullable
    @Contract("null, _ -> null")
    public static <T extends PsiElement> T getNextSiblingOfAnyType(
        @Nullable PsiElement sibling,
        @NotNull Class... aClasses
    ) {
        if (sibling == null) {
            return null;
        } else {
            for (PsiElement child = sibling.getNextSibling(); child != null; child = child.getNextSibling()) {
                for (final Class<T> aClass : aClasses) {
                    if (aClass.isInstance(child)) {
                        return (T) child;
                    }
                }
            }

            return null;
        }
    }

    @Nullable
    @Contract("null, _ -> null")
    public static PsiElement findSiblingByPredicate(
        @Nullable final PsiElement sibling,
        @NotNull final Predicate<PsiElement> predicate
    ) {
        if (sibling == null) {
            return null;
        } else {
            for (PsiElement child = sibling.getNextSibling(); child != null; child = child.getNextSibling()) {
                if (predicate.test(child)) {
                    return child;
                }
            }

            return null;
        }
    }


    @Contract(pure = true)
    public static boolean aroundIsValueLine(@Nullable final PsiElement element) {
        if (element == null) {
            return false;
        }

        if (isImpexValueLine(element)
            || isImpexValueLine(element.getParent())
            || isImpexValueLine(element.getParent().getParent())) {
            return true;
        }

        boolean prevElementIsValueLine = false;
        PsiElement prevSibling = PsiTreeUtil.skipSiblingsBackward(
            element,
            PsiWhiteSpace.class,
            PsiComment.class,
            ImpexBeanShell.class,
            ImpexComment.class
        );
        while (null != prevSibling) {
            if (isHeaderLine(prevSibling) || isImpexValueLine(prevSibling) || isUserRightsMacros(prevSibling)) {
                prevElementIsValueLine = true;
                break;
            }
            prevSibling = PsiTreeUtil.skipSiblingsBackward(
                prevSibling,
                PsiWhiteSpace.class,
                PsiComment.class,
                ImpexBeanShell.class,
                ImpexComment.class
            );
        }


        boolean nextElementIsValueLine = false;
        PsiElement nextSibling = PsiTreeUtil.skipSiblingsForward(element,
                                                                 PsiWhiteSpace.class, ImpexBeanShell.class,
                                                                 ImpexComment.class
        );

        while (null != nextSibling) {
            if (isImpexValueLine(nextSibling)) {
                nextElementIsValueLine = true;
                break;
            }
            if (isHeaderLine(nextSibling)) {
                nextElementIsValueLine = false;
                break;
            }
            if (isUserRightsMacros(nextSibling)) {
                nextElementIsValueLine = false;
                break;
            }
            nextSibling = PsiTreeUtil.skipSiblingsForward(nextSibling,
                                                          PsiWhiteSpace.class, ImpexBeanShell.class,
                                                          ImpexComment.class
            );
        }

        return nextElementIsValueLine && prevElementIsValueLine;
    }

    @Nullable
    @Contract(pure = true)
    public static boolean nextElementIsHeaderLine(@NotNull final PsiElement element) {
        Validate.notNull(element);

        PsiElement nextSibling = element.getNextSibling();

        while (null != nextSibling) {
            if (isImpexValueLine(nextSibling)) {
                return false;
            }
            if (isUserRightsMacros(nextSibling)) {
                return false;
            }
            if (isHeaderLine(nextSibling)) {
                return true;
            }
            nextSibling = nextSibling.getNextSibling();
        }

        return true;
    }

    @Nullable
    @Contract(pure = true)
    public static boolean nextElementIsUserRightsMacros(@NotNull final PsiElement element) {
        Validate.notNull(element);

        PsiElement nextSibling = element.getNextSibling();

        while (null != nextSibling) {
            if (isHeaderLine(nextSibling)) {
                return false;
            }
            if (isImpexValueLine(nextSibling)) {
                return false;
            }
            if (isUserRightsMacros(nextSibling)) {
                return true;
            }
            nextSibling = nextSibling.getNextSibling();
        }

        return true;
    }

    @Nullable
    @Contract(pure = true)
    public static boolean prevElementIsUserRightsMacros(@NotNull final PsiElement element) {
        Validate.notNull(element);

        final Class[] skipClasses = {ImpexValueLine.class, PsiComment.class, PsiWhiteSpace.class};
        PsiElement prevElement = PsiTreeUtil.skipSiblingsBackward(element, skipClasses);

        while (null != prevElement) {
            if (isHeaderLine(prevElement)) {
                return false;
            }
            if (isUserRightsMacros(prevElement)) {
                return true;
            }
            prevElement = PsiTreeUtil.skipSiblingsBackward(prevElement, skipClasses);
        }

        return false;
    }

    @Nullable
    @Contract(pure = true)
    public static PsiElement getPrevNonWhitespaceElement(@NotNull final PsiElement element) {
        Validate.notNull(element);

        PsiElement prevSibling = element.getPrevSibling();

        while (null != prevSibling && (isWhiteSpace(prevSibling) || isLineBreak(prevSibling))) {
            prevSibling = prevSibling.getPrevSibling();
        }

        return prevSibling;
    }

    @Nullable
    @Contract(pure = true)
    public static PsiElement getNextNonWhitespaceElement(@NotNull final PsiElement element) {
        Validate.notNull(element);

        PsiElement nextSibling = element.getNextSibling();

        while (null != nextSibling && (isWhiteSpace(nextSibling) || isLineBreak(nextSibling))) {
            nextSibling = nextSibling.getNextSibling();
        }

        return nextSibling;
    }


    @Nullable
    @Contract(pure = true)
    public static PsiElement getPrevValueLine(@NotNull final PsiElement element) {
        Validate.notNull(element);

        PsiElement prevSibling = element.getPrevSibling();

        while (null != prevSibling && !isImpexValueLine(prevSibling)) {
            prevSibling = prevSibling.getPrevSibling();
        }

        return prevSibling;
    }


    public static PsiElement getHeaderOfValueGroupUnderCaret(@NotNull final Editor editor) {
        Validate.notNull(editor);

        final PsiElement psiElementUnderCaret = PsiUtilBase.getElementAtCaret(editor);
        if (null == psiElementUnderCaret) {
            return null;
        }

        final ImpexValueGroup valueGroup = getClosestSelectedValueGroupFromTheSameLine(
            psiElementUnderCaret);
        if (null != valueGroup) {

            final PsiElement header = getHeaderForValueGroup(valueGroup);
            if (null != header) {
                return header;
            }
        }

        return null;
    }

    public static List<PsiElement> getColumnOfHeaderUnderCaret(@NotNull final Editor editor) {
        Validate.notNull(editor);

        final PsiElement psiElementUnderCaret = PsiUtilBase.getElementAtCaret(editor);
        if (null == psiElementUnderCaret) {
            return null;
        }

        final ImpexFullHeaderParameter headerParameter = PsiTreeUtil.getParentOfType(
            psiElementUnderCaret,
            ImpexFullHeaderParameter.class
        );
        if (null != headerParameter) {
            return getColumnForHeader(headerParameter);
        }

        return null;
    }

    @NotNull
    public static List<PsiElement> getColumnForHeader(@NotNull final ImpexFullHeaderParameter headerParameter) {
        final PsiElement[] children = headerParameter.getParent().getChildren();
        int i = -2;
        for (final PsiElement child : children) {
            if (!child.equals(headerParameter)) {
                i++;
            } else {
                break;
            }
        }

        final List<PsiElement> result = new ArrayList<>();
        PsiElement psiElement = getNextSiblingOfAnyType(
            PsiTreeUtil.getParentOfType(headerParameter, ImpexHeaderLine.class),
            ImpexValueLine.class,
            ImpexHeaderLine.class,
            ImpexRootMacroUsage.class
        );

        while (psiElement != null && !isHeaderLine(psiElement) && !isUserRightsMacros(psiElement)) {
            if (isImpexValueLine(psiElement)) {
                final PsiElement[] elements = psiElement.getChildren();
                if (elements.length > i) {
                    result.add(elements[i]);
                }
            }


            psiElement = getNextSiblingOfAnyType(
                psiElement,
                ImpexValueLine.class,
                ImpexHeaderLine.class,
                ImpexRootMacroUsage.class
            );
        }

        return result;
    }

    @Nullable
    @Contract(pure = true)
    public static ImpexValueGroup getClosestSelectedValueGroupFromTheSameLine(@Nullable final PsiElement psiElementUnderCaret) {
        if (null == psiElementUnderCaret) {
            return null;
        }

        if (psiElementUnderCaret instanceof ImpexValueGroup) {
            return (ImpexValueGroup) psiElementUnderCaret;
        } else if (isFieldValueSeparator(psiElementUnderCaret)) {

            final ImpexValueGroup valueGroup = PsiTreeUtil.getParentOfType(psiElementUnderCaret, ImpexValueGroup.class);
            if (null != valueGroup) {
                return PsiTreeUtil.getPrevSiblingOfType(valueGroup, ImpexValueGroup.class);
            }

        } else if (isWhiteSpace(psiElementUnderCaret)) {

            ImpexValueGroup valueGroup = PsiTreeUtil.getParentOfType(psiElementUnderCaret, ImpexValueGroup.class);

            if (null == valueGroup) {
                valueGroup = PsiTreeUtil.getPrevSiblingOfType(psiElementUnderCaret, ImpexValueGroup.class);
            }

            if (null == valueGroup) {
                valueGroup = skipAllExceptLineBreaksAndGetImpexValueGroup(psiElementUnderCaret);
            }

            return valueGroup;

        } else if (isLineBreak(psiElementUnderCaret)) {

            return skipAllExceptLineBreaksAndGetImpexValueGroup(psiElementUnderCaret);

        } else {
            return PsiTreeUtil.getParentOfType(psiElementUnderCaret, ImpexValueGroup.class);
        }

        return null;
    }

    @Nullable
    @Contract(pure = true)
    public static PsiElement getHeaderForValueGroup(@Nullable final ImpexValueGroup valueGroup) {
        if (null == valueGroup) {
            return null;
        }

        final int columnNumber = getColumnNumber(valueGroup);

        if (columnNumber < 0) {
            return null;
        }

        final ImpexValueLine impexValueLine = valueGroup.getValueLine();
        if (null == impexValueLine) {
            return null;
        }

        if (prevElementIsUserRightsMacros(impexValueLine)) {
            return null;
        }

        final ImpexHeaderLine impexHeaderLine = PsiTreeUtil.getPrevSiblingOfType(impexValueLine, ImpexHeaderLine.class);
        if (null == impexHeaderLine) {
            return null;
        }

        final ImpexFullHeaderParameter header = getImpexFullHeaderParameterFromHeaderLineByNumber(
            columnNumber,
            impexHeaderLine
        );

        if (null == header) {
            return getHeaderParametersSeparatorFromHeaderLineByNumber(columnNumber, impexHeaderLine);
        } else {
            return header;
        }
    }

    @Nullable
    @Contract(pure = true)
    public static ImpexValueGroup skipAllExceptLineBreaksAndGetImpexValueGroup(
        @NotNull final PsiElement psiElement
    ) {
        Validate.notNull(psiElement);

        if (isLineBreak(psiElement.getPrevSibling())) {
            return null;
        }

        PsiElement prevSibling = psiElement.getPrevSibling();
        while (!isImpexValueLine(prevSibling)) {
            if (null == prevSibling || isLineBreak(prevSibling)) {
                return null;
            }

            prevSibling = prevSibling.getPrevSibling();
        }

        if (!isImpexValueLine(prevSibling)) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(PsiTreeUtil.lastChild(prevSibling), ImpexValueGroup.class);
    }

    @Contract(pure = true)
    public static int getColumnNumber(@NotNull final ImpexValueGroup valueGroup) {
        Validate.notNull(valueGroup);

        final List<ImpexValueGroup> valueGroups = PsiTreeUtil.getChildrenOfTypeAsList(valueGroup.getValueLine(), ImpexValueGroup.class);

        int columnNumber = 0;

        for (ImpexValueGroup group : valueGroups) {
            if (group == valueGroup) {
                return columnNumber;
            }

            columnNumber++;
        }

        return -1;
    }

    @Contract(pure = true)
    public static int getColumnNumber(@NotNull final ImpexFullHeaderParameter element) {
        final List<ImpexFullHeaderParameter> groups = PsiTreeUtil.getChildrenOfTypeAsList(element.getHeaderLine(), ImpexFullHeaderParameter.class);

        int columnNumber = 0;

        for (ImpexFullHeaderParameter group : groups) {
            if (group == element) {
                return columnNumber;
            }

            columnNumber++;
        }

        return -1;
    }

    @Nullable
    @Contract(pure = true)
    public static ImpexFullHeaderParameter getImpexFullHeaderParameterFromHeaderLineByNumber(
        final int columnNumber,
        @NotNull final ImpexHeaderLine impexHeaderLine
    ) {
        Validate.isTrue(columnNumber >= 0);
        Validate.notNull(impexHeaderLine);

        final PsiElement columnSeparator = getHeaderParametersSeparatorFromHeaderLineByNumber(
            columnNumber,
            impexHeaderLine
        );
        if (null == columnSeparator) {
            return null;
        }

        final PsiElement nextSibling = CommonPsiUtils.getNextNonWhitespaceElement(columnSeparator);

        if (isImpexFullHeaderParameter(nextSibling)) {
            return (ImpexFullHeaderParameter) nextSibling;
        }

        return null;
    }

    @Nullable
    @Contract(pure = true)
    public static PsiElement getHeaderParametersSeparatorFromHeaderLineByNumber(
        final int columnNumber,
        @NotNull final ImpexHeaderLine impexHeaderLine
    ) {
        Validate.isTrue(columnNumber >= 0);
        Validate.notNull(impexHeaderLine);

        final List<PsiElement> parameterSeparators = CommonPsiUtils.findChildrenByIElementType(
            impexHeaderLine, ImpexTypes.PARAMETERS_SEPARATOR
        );

        if (columnNumber >= parameterSeparators.size()) {
            return null;
        }

        return parameterSeparators.get(columnNumber);
    }

}
