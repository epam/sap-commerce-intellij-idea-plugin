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

package com.intellij.idea.plugin.hybris.impex.lang.folding.simple;

import com.intellij.idea.plugin.hybris.impex.lang.folding.ImpexFoldingPlaceholderBuilder;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Created 23:16 01 January 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultImpexFoldingPlaceholderBuilder implements ImpexFoldingPlaceholderBuilder {

    @NotNull
    @Override
    public String getPlaceholder(@NotNull final PsiElement psiElement) {
        Validate.notNull(psiElement);

        return this.getFirstAndLastCharacters(psiElement);
    }

    @NotNull
    @Contract(pure = true)
    private String getFirstAndLastCharacters(@NotNull final PsiElement psiElement) {
        Validate.notNull(psiElement);

        final String text = psiElement.getText();

        if (text.length() < 2) {
            return psiElement.getText();
        }

        return String.valueOf(text.charAt(0)) + text.charAt(text.length() - 1);
    }
}
