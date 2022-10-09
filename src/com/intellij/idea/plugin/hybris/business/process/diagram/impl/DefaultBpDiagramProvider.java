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

package com.intellij.idea.plugin.hybris.business.process.diagram.impl;

import com.intellij.diagram.DiagramColorManager;
import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramElementManager;
import com.intellij.diagram.DiagramPresentationModel;
import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.idea.plugin.hybris.business.process.common.BpGraphNode;
import com.intellij.idea.plugin.hybris.business.process.diagram.BpDiagramColorManager;
import com.intellij.idea.plugin.hybris.business.process.diagram.BpDiagramElementManager;
import com.intellij.idea.plugin.hybris.business.process.diagram.BpDiagramProvider;
import com.intellij.idea.plugin.hybris.business.process.diagram.BpDiagramVfsResolver;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 11:08 PM 31 January 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultBpDiagramProvider extends BpDiagramProvider {

    private static final Logger LOG = Logger.getInstance(DefaultBpDiagramProvider.class);

    public static final String ID = "HybrisBusinessProcessDiagramProvider";

    @Pattern("[a-zA-Z0-9_-]*")
    @Override
    public String getID() {
        return ID;
    }

    @Override
    public DiagramElementManager<BpGraphNode> getElementManager() {
        return ApplicationManager.getApplication().getService(BpDiagramElementManager.class);
    }

    @Override
    public DiagramVfsResolver<BpGraphNode> getVfsResolver() {
        return ApplicationManager.getApplication().getService(BpDiagramVfsResolver.class);
    }

    @Override
    public String getPresentableName() {
        return HybrisI18NBundleUtils.message("hybris.business.process.provider.name");
    }

    @Override
    public DiagramDataModel<BpGraphNode> createDataModel(
        @NotNull final Project project,
        @Nullable final BpGraphNode t,
        @Nullable final VirtualFile virtualFile,
        final DiagramPresentationModel diagramPresentationModel
    ) {
        final BpDiagramDataModel bpDiagramDataModel = new BpDiagramDataModel(project, t);

        bpDiagramDataModel.refreshDataModel();

        return bpDiagramDataModel;
    }

    @Override
    public DiagramColorManager getColorManager() {
        return ApplicationManager.getApplication().getService(BpDiagramColorManager.class);
    }
}
