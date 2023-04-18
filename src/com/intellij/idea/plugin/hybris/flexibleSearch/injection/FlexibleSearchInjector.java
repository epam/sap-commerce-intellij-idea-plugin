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

package com.intellij.idea.plugin.hybris.flexibleSearch.injection;

import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexStringImpl;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
public class FlexibleSearchInjector implements LanguageInjector {

    private static final Logger LOG = Logger.getInstance(FlexibleSearchInjector.class);

    @Override
    public void getLanguagesToInject(
        @NotNull final PsiLanguageInjectionHost host,
        @NotNull final InjectedLanguagePlaces injectionPlacesRegistrar
    ) {
        final PsiElement hostParent = host.getParent();
        if (host instanceof ImpexStringImpl) {
            final String hostString = StringUtil.unquoteString(host.getText()).toLowerCase();
            if (StringUtil.trim(hostString).startsWith("select ")) {
                registerInjectionPlace(injectionPlacesRegistrar, host);
            }
        }

        if (hostParent != null) {
            if (hostParent.getParent() instanceof PsiMethodCallExpressionImpl) {
                final PsiMethodCallExpressionImpl callExpression = (PsiMethodCallExpressionImpl) hostParent.getParent();
                final PsiMethod method = callExpression.resolveMethod();
                if (method != null) {
                    final PsiClass containingClass = method.getContainingClass();
                    if (containingClass != null
                        && "FlexibleSearchService".equals(containingClass.getName())
                        && "search".equals(method.getName())) {

                        registerInjectionPlace(injectionPlacesRegistrar, host);
                    }
                }
            }
        }
    }

    private void registerInjectionPlace(
        @NotNull final InjectedLanguagePlaces injectionPlacesRegistrar,
        @NotNull final PsiElement host
    ) {
        final Language language = FlexibleSearchLanguage.Companion.getINSTANCE();
        try {
            injectionPlacesRegistrar.addPlace(
                language,
                TextRange.from(1, host.getTextLength() - 2),
                null,
                null
            );
        } catch (ProcessCanceledException e) {
            // ignore
        } catch (Throwable e) {
            LOG.error(e);
        }
    }
}