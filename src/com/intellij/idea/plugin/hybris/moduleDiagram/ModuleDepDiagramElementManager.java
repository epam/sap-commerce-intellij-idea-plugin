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

package com.intellij.idea.plugin.hybris.moduleDiagram;

import com.intellij.diagram.AbstractDiagramElementManager;
import com.intellij.diagram.DiagramBuilder;
import com.intellij.idea.plugin.hybris.actions.ActionUtils;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.ui.SimpleColoredText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Eugene.Kudelevsky
 */
@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
public class ModuleDepDiagramElementManager extends AbstractDiagramElementManager<ModuleDepDiagramItem> {

    @Nullable
    @Override
    public ModuleDepDiagramItem findInDataContext(final DataContext dataContext) {
        return new ModuleDepDiagramItem(null, false);
    }

    @NotNull
    @Override
    public Collection<ModuleDepDiagramItem> findElementsInDataContext(final DataContext context) {
        return new ArrayList<>(  // need to return mutable collection to avoid UOE from the platform
            ActionUtils.isHybrisContext(context)
                ? Collections.singletonList(new ModuleDepDiagramItem(null, false))
                : Collections.emptyList());
    }

    @Override
    public boolean isAcceptableAsNode(final Object element) {
        return element instanceof ModuleDepDiagramItem;
    }

    @Nullable
    @Override
    public String getElementTitle(final ModuleDepDiagramItem element) {
        return element.toString();
    }

    @Override
    public @Nullable SimpleColoredText getItemName(
        @Nullable final ModuleDepDiagramItem nodeElement,
        @Nullable final Object nodeItem,
        @NotNull final DiagramBuilder builder
    ) {
        return nodeElement != null
            ? new SimpleColoredText(nodeElement.getName(), DEFAULT_TITLE_ATTR)
            : null;
    }

    @Override
    public String getNodeTooltip(final ModuleDepDiagramItem element) {
        return element.getName();
    }
}
