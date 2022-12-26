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

package com.intellij.idea.plugin.hybris.psi.references;

import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaItemService;
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public abstract class TSReferenceBase<PSI extends PsiElement> extends PsiReferenceBase.Poly<PSI> {

    protected static final Object[] NO_VARIANTS = new Object[0];

    public TSReferenceBase(@NotNull final PSI owner) {
        super(owner, false);
    }

    public TSReferenceBase(final PSI element, final TextRange rangeInElement) {
        super(element, rangeInElement, false);
    }

    @Override
    protected TextRange calculateDefaultRangeInElement() {
        return TextRange.from(0, getElement().getTextLength());
    }

    @NotNull
    @Override
    public final Object[] getVariants() {
        return NO_VARIANTS;
    }

    @NotNull
    protected final Project getProject() {
        return getElement().getProject();
    }

    @NotNull
    protected final TSMetaModelAccess getMetaModelAccess() {
        return TSMetaModelAccess.Companion.getInstance(getProject());
    }

    @NotNull
    protected final TSMetaItemService getMetaItemService() {
        return TSMetaItemService.getInstance(getProject());
    }

    public interface TSResolveResult extends ResolveResult {

        @NotNull
        DomElement getSemanticDomElement();

    }
}
