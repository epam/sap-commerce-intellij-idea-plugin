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

import com.intellij.diagram.AbstractDiagramElementManager;
import com.intellij.idea.plugin.hybris.business.process.common.BpGraphNode;
import com.intellij.idea.plugin.hybris.business.process.common.BpGraphService;
import com.intellij.idea.plugin.hybris.business.process.diagram.BpDiagramElementManager;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.UnmarshalException;


/**
 * Created 11:12 PM 31 January 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class BpDiagramElementManagerIml extends AbstractDiagramElementManager<BpGraphNode>
    implements BpDiagramElementManager {

    private static final Logger LOG = Logger.getInstance(BpDiagramElementManagerIml.class);

    @Nullable
    @Override
    public BpGraphNode findInDataContext(final DataContext dataContext) {
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final CommonIdeaService commonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService.class);
        if (!commonIdeaService.isHybrisProject(project)) {
            return null;
        }

        final VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);

        if (null == virtualFile) {
            return null;
        }

        if (!virtualFile.getName().endsWith("process.xml")) {
            return null;
        }

        final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);

        if (!(psiFile instanceof XmlFile) ||
            psiFile instanceof HtmlFileImpl) { // but psiFile must not be html.
            return null;
        }

        final BpGraphService bpGraphService = ApplicationManager.getApplication().getService(BpGraphService.class);

        try {
            return bpGraphService.buildGraphFromXmlFile(virtualFile);
        } catch (UnmarshalException e) {
            return null;
        }
    }

    @Override
    public boolean isAcceptableAsNode(final Object o) {
        return o instanceof BpGraphNode;
    }

    @Nullable
    @Override
    public String getElementTitle(final BpGraphNode t) {
        return t.getGenericAction().getId();
    }

    @Override
    public String getNodeTooltip(final BpGraphNode t) {
        return t.getGenericAction().getId();
    }
}
