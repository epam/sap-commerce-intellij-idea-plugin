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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.idea.plugin.hybris.project.configurators.ConfiguratorFactory;
import com.intellij.idea.plugin.hybris.project.configurators.MavenConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.MavenModuleDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.wizards.MavenProjectBuilder;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultMavenConfigurator implements MavenConfigurator {

    @Override
    public void configure(
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final Project project,
        @NotNull final List<MavenModuleDescriptor> mavenModules,
        @NotNull final ConfiguratorFactory configuratorFactory
    ) {
        final List<VirtualFile> mavenProjectFiles = mavenModules
            .stream()
            .map(e -> new File(e.getModuleRootDirectory(), MavenConstants.POM_XML))
            .map(e -> VfsUtil.findFileByIoFile(e, true))
            .toList();

        final var mavenProjectBuilders = mavenProjectFiles.stream()
                                                          .map(mavenProjectBuilderFunction(project))
                                                          .filter(isProjectPathValid(mavenModules))
                                                          .toList();

        mavenProjectBuilders.forEach(builder -> {
            try {
                builder.commit(project, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
            } finally {
                builder.cleanup();
            }
        });

        MavenProjectsManager.getInstance(project).importProjects();
    }

    @NotNull
    private Function<VirtualFile, MavenProjectBuilder> mavenProjectBuilderFunction(final @NotNull Project project) {
        return mavenProjectFile -> {
            final var builder = new MavenProjectBuilder();
            builder.setUpdate(MavenProjectsManager.getInstance(project).isMavenizedProject());
            builder.setFileToImport(mavenProjectFile);
            return builder;
        };
    }

    @NotNull
    private Predicate<MavenProjectBuilder> isProjectPathValid(final @NotNull List<MavenModuleDescriptor> mavenModules) {
        return builder -> {
            final var path = builder.getRootPath()
                                    .toAbsolutePath()
                                    .toString();
            return mavenModules.stream()
                               .anyMatch(module -> module.getModuleRootDirectory()
                                                         .getAbsolutePath()
                                                         .equals(path));
        };
    }

}
