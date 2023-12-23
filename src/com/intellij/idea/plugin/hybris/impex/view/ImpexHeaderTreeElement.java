/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.impex.view;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderMode;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class ImpexHeaderTreeElement extends PsiTreeElementBase<ImpexHeaderLine> implements ItemPresentation {

    protected ImpexHeaderTreeElement(final ImpexHeaderLine psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        final var element = getElement();
        if (element == null) return Collections.emptyList();

        return getElement().getFullHeaderParameterList()
            .stream()
            .map(ImpexHeaderParameterElement::new)
            .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public String getPresentableText() {
        final var element = getElement();
        if (element == null) return null;

        final ImpexFullHeaderType fullHeaderType = getElement().getFullHeaderType();
        if (fullHeaderType == null) return null;

        return fullHeaderType.getText();
    }

    @Nullable
    @Override
    public String getLocationString() {
        final var element = getElement();

        if (element == null) return null;

        final ImpexAnyHeaderMode mode = element.getAnyHeaderMode();
        return mode.getText();
    }

    @Override
    @Nullable
    public Icon getIcon(final boolean open) {
        return null;
    }
}
