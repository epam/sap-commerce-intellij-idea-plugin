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

import com.intellij.diagram.DiagramColorManager;
import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramElementManager;
import com.intellij.diagram.DiagramNodeContentManager;
import com.intellij.diagram.DiagramPresentationModel;
import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.DiagramRelationshipManager;
import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.diagram.DiagramVisibilityManager;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Eugene.Kudelevsky
 */
@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
public class ModuleDepDiagramProvider extends DiagramProvider<ModuleDepDiagramItem> {

    private final ModuleDepDiagramElementManager myElementManager = new ModuleDepDiagramElementManager();
    private final ModuleDepDiagramNodeContentManager myNodeContentManager = new ModuleDepDiagramNodeContentManager();
    private final ModuleDepDiagramVfsResolver myVfsResolver = new ModuleDepDiagramVfsResolver();
    private final ModuleDepDiagramColorManager myColorManager = new ModuleDepDiagramColorManager();

    @Pattern("[a-zA-Z0-9_-]*")
    @Override
    public String getID() {
        return "HybrisModuleDependencies";
    }

    @Override
    @NotNull
    public DiagramVisibilityManager createVisibilityManager() {
        return new ModuleDepDiagramVisibilityManager();
    }

    @Override
    @NotNull
    public DiagramNodeContentManager createNodeContentManager() {
        return myNodeContentManager;
    }

    @Override
    @NotNull
    public DiagramElementManager<ModuleDepDiagramItem> getElementManager() {
        return myElementManager;
    }

    @Override
    @NotNull
    public DiagramVfsResolver<ModuleDepDiagramItem> getVfsResolver() {
        return myVfsResolver;
    }

    @Override
    @NotNull
    public DiagramColorManager getColorManager() {
        return myColorManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public DiagramRelationshipManager<ModuleDepDiagramItem> getRelationshipManager() {
        return (DiagramRelationshipManager<ModuleDepDiagramItem>) DiagramRelationshipManager.NO_RELATIONSHIP_MANAGER;
    }

    @Override
    @NotNull
    public String getPresentableName() {
        return HybrisI18NBundleUtils.message("hybris.module.dependencies.diagram.provider.name");
    }

    @Override
    @NotNull
    public Icon getActionIcon(final boolean isPopup) {
        return HybrisIcons.HYBRIS_ICON;
    }

    @Override
    @NotNull
    public DiagramDataModel<ModuleDepDiagramItem> createDataModel(
        @NotNull final Project project,
        @Nullable final ModuleDepDiagramItem item,
        @Nullable final VirtualFile file,
        final DiagramPresentationModel model
    ) {
        return new ModuleDepDiagramDataModel(project, this);
    }

}
