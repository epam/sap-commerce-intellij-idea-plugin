/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.psi.reference;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

public abstract class TSReferenceBase<PSI extends PsiElement> extends PsiReferenceBase.Poly<PSI> {

    public TSReferenceBase(@NotNull final PSI owner) {
        super(owner, false);
    }

    public TSReferenceBase(@NotNull final PSI owner, final boolean soft) {
        super(owner, soft);
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
    public Object[] getVariants() {
        return ResolveResult.EMPTY_ARRAY;
    }

    @NotNull
    protected final Project getProject() {
        return getElement().getProject();
    }

    public interface TSResolveResult extends ResolveResult {

    }
}
