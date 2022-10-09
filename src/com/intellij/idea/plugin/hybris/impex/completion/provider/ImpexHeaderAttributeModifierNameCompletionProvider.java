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

package com.intellij.idea.plugin.hybris.impex.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier;
import com.intellij.idea.plugin.hybris.impex.constants.modifier.ImpexModifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * Created 22:11 14 May 2016
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class ImpexHeaderAttributeModifierNameCompletionProvider extends CompletionProvider<CompletionParameters> {

    @NotNull
    public static CompletionProvider<CompletionParameters> getInstance() {
        return ApplicationManager.getApplication().getService(ImpexHeaderAttributeModifierNameCompletionProvider.class);
    }

    @Override
    public void addCompletions(
        @NotNull final CompletionParameters parameters,
        final ProcessingContext context,
        @NotNull final CompletionResultSet result
    ) {
        Validate.notNull(parameters);
        Validate.notNull(result);

        for (ImpexModifier impexModifier : AttributeModifier.values()) {
            result.addElement(LookupElementBuilder.create(impexModifier.getModifierName()));
        }
    }
}
