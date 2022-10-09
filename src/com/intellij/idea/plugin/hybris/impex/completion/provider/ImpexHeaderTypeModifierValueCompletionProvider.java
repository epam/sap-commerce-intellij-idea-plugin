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
import com.intellij.icons.AllIcons;
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage;
import com.intellij.idea.plugin.hybris.impex.constants.modifier.ImpexModifier;
import com.intellij.idea.plugin.hybris.impex.constants.modifier.ImpexModifierValue;
import com.intellij.idea.plugin.hybris.impex.constants.modifier.TypeModifier;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.intellij.idea.plugin.hybris.impex.constants.modifier.ImpexProcessorModifier.ImpexProcessorModifierValue;
import static com.intellij.idea.plugin.hybris.impex.constants.modifier.TypeModifier.PROCESSOR;

/**
 * Created 22:12 14 May 2016
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class ImpexHeaderTypeModifierValueCompletionProvider extends CompletionProvider<CompletionParameters> {

    @NotNull
    public static CompletionProvider<CompletionParameters> getInstance() {
        return ApplicationManager.getApplication().getService(ImpexHeaderTypeModifierValueCompletionProvider.class);
    }

    @Override
    public void addCompletions(
        @NotNull final CompletionParameters parameters,
        final ProcessingContext context,
        @NotNull final CompletionResultSet result
    ) {
        Validate.notNull(parameters);
        Validate.notNull(result);

        final PsiElement psiElementUnderCaret = parameters.getPosition();

        final ImpexAttribute impexAttribute = PsiTreeUtil.getParentOfType(
            psiElementUnderCaret, ImpexAttribute.class
        );

        if (impexAttribute == null) {
            return;
        }

        final String modifierName = impexAttribute.getAnyAttributeName().getText();

        final ImpexModifier impexModifier = TypeModifier.getByModifierName(modifierName);

        if (null != impexModifier) {

            if (PROCESSOR.equals(impexModifier)) {
                final ImpexModifierValue[] modifierValues = ((TypeModifier) impexModifier).getRawModifierValues();
                Arrays.stream(modifierValues).forEach(it -> {
                    final PsiClass psiClass = ((ImpexProcessorModifierValue) it).getPsiClass();
                    final LookupElementBuilder lookup = LookupElementBuilder.create(psiClass.getQualifiedName())
                                                                            .withPresentableText(psiClass.getName())
                                                                            .withIcon(AllIcons.Nodes.Class);
                    result.addElement(lookup);
                });

            } else {
                for (String possibleValue : impexModifier.getModifierValues()) {
                    result.addElement(LookupElementBuilder.create(possibleValue));
                }
            }

        } else {
            // show error message when not defined within hybris API
            Notifications.Bus.notify(new Notification(
                ImpexLanguage.getInstance().getDisplayName(),
                "possible error in your impex",
                "You typed an unknown hybris-TYPE-modifier with name '" + modifierName + "'.",
                NotificationType.WARNING
            ));
        }
    }
}
