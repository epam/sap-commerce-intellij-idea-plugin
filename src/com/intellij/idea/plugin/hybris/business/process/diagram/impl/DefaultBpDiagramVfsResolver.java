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

import com.intellij.idea.plugin.hybris.business.process.common.BpGraphNode;
import com.intellij.idea.plugin.hybris.business.process.common.BpGraphService;
import com.intellij.idea.plugin.hybris.business.process.diagram.BpDiagramVfsResolver;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jgoodies.common.base.Objects;
import org.jetbrains.annotations.Nullable;

import jakarta.xml.bind.UnmarshalException;

/**
 * Created 11:12 PM 31 January 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultBpDiagramVfsResolver implements BpDiagramVfsResolver {

    private static final Logger LOG = Logger.getInstance(DefaultBpDiagramVfsResolver.class);

    @Override
    public String getQualifiedName(final BpGraphNode t) {

        if (Objects.equals(t.getProcess().getStart(), (t.getGenericAction().getId()))) {
            return t.getXmlVirtualFile().getUrl();
        }

        return null;
    }

    @Nullable
    @Override
    public BpGraphNode resolveElementByFQN(final String s, final Project project) {
        final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(s);

        final BpGraphService bpGraphService = ApplicationManager.getApplication().getService(BpGraphService.class);

        try {
            return null == virtualFile ? null : bpGraphService.buildGraphFromXmlFile(virtualFile);
        } catch (UnmarshalException e) {
            LOG.error("Can not build Business Process graph from the file: " + virtualFile.getName(), e);
            return null;
        }
    }
}
