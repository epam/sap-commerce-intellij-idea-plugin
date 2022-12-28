/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.psi.reference.provider;

import com.intellij.idea.plugin.hybris.psi.reference.HybrisModelItemReference;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nosov Aleksandr
 */
public class HybrisModelItemReferenceProvider extends PsiReferenceProvider {

    private final static Logger LOG = Logger.getInstance(
        "#com.intellij.idea.plugin.hybris.reference.contributor.HybrisItemValueReferenceProvider");

    @Override
    @NotNull
    public final PsiReference[] getReferencesByElement(
        @NotNull final PsiElement element,
        @NotNull final ProcessingContext context
    ) {

        final HybrisModelItemReference reference
            = new HybrisModelItemReference(element, true);
        final List<PsiReference> results = new ArrayList<>();
        results.add(reference);
        return results.toArray(new PsiReference[results.size()]);
    }
}