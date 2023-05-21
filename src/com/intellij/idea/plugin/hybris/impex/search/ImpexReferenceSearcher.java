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

package com.intellij.idea.plugin.hybris.impex.search;

import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexMacroReference;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isMacroNameDeclaration;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isMacroUsage;
import static java.util.Arrays.stream;

/**
 * Searcher class for search psi reference among impex's file.
 *
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
public class ImpexReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {

    public ImpexReferenceSearcher() {
        super(true);
    }

    @Override
    public void processQuery(
        @NotNull final ReferencesSearch.SearchParameters queryParameters,
        @NotNull final Processor<? super PsiReference> consumer
    ) {
        final PsiElement elementToSearch = queryParameters.getElementToSearch();
        if (!elementToSearch.isValid()) {
            return;
        }

        final SearchScope scope = queryParameters.getEffectiveSearchScope();
        if (!(scope instanceof GlobalSearchScope)) {
            return;
        }

        // search macros usage references
        if (isMacroUsage(elementToSearch) || isMacroNameDeclaration(elementToSearch)) {
            final PsiFile file = elementToSearch.getContainingFile();
            final PsiElement[] results = PsiTreeUtil.collectElements(
                file,
                element -> isMacroUsage(element) && element.textMatches(elementToSearch)
            );
            stream(results)
                .map(ImpexMacroReference::new)
                .forEach(consumer::process);
        }
    }
}
