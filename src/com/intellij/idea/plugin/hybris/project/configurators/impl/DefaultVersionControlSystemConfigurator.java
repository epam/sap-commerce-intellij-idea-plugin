/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2014-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.idea.plugin.hybris.project.configurators.VersionControlSystemConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vcs.roots.VcsRootDetector;
import com.intellij.openapi.vfs.VfsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;

public class DefaultVersionControlSystemConfigurator implements VersionControlSystemConfigurator {

    @Override
    public void configure(
        final @NotNull ProgressIndicator indicator, @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final Project project
    ) {
        indicator.setText(message("hybris.project.import.vcs"));

        final var vcsManager = ProjectLevelVcsManager.getInstance(project);
        final var rootDetector = project.getService(VcsRootDetector.class);
        final var detectedRoots = new HashSet<>(rootDetector.detect());

        for (final var vcs : hybrisProjectDescriptor.getDetectedVcs()) {
            final var vf = VfsUtil.findFileByIoFile(vcs, true);
            final var roots = rootDetector.detect(vf);
            detectedRoots.addAll(roots);
        }
        final var directoryMappings = detectedRoots.stream()
            .filter(root -> root.getVcs() != null)
            .map(root -> new VcsDirectoryMapping(root.getPath().getPath(), root.getVcs().getName()))
            .collect(Collectors.toCollection(ArrayList::new));
        vcsManager.setDirectoryMappings(directoryMappings);
    }
}
