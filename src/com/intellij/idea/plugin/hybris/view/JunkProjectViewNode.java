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

package com.intellij.idea.plugin.hybris.view;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class JunkProjectViewNode extends ProjectViewNode<List<AbstractTreeNode<?>>> {

    public JunkProjectViewNode(
        final Project project,
        final List<AbstractTreeNode<?>> children,
        final ViewSettings viewSettings
    ) {
        super(project, children, viewSettings);
    }

    @Override
    @NotNull
    public Collection<AbstractTreeNode<?>> getChildren() {
        return this.getValue();
    }

    @Override
    public boolean contains(@NotNull final VirtualFile file) {
        for (AbstractTreeNode abstractTreeNode : this.getValue()) {
            if (abstractTreeNode instanceof final ProjectViewNode projectViewNode) {

                if (null == projectViewNode.getVirtualFile()) {
                    continue;
                }

                if (projectViewNode.getVirtualFile().equals(file)) {
                    return true;
                }

                if (file.getPath().startsWith(projectViewNode.getVirtualFile().getPath())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void update(final PresentationData presentation) {
        presentation.setIcon(AllIcons.Modules.ExcludedGeneratedRoot);
        presentation.addText(new ColoredFragment(
            HybrisI18NBundleUtils.message("hybris.project.view.junk.directory.name"),
            SimpleTextAttributes.EXCLUDED_ATTRIBUTES
        ));
    }
}
