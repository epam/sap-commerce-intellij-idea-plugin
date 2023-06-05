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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.idea.plugin.hybris.project.configurators.EclipseConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.AbstractModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.EclipseModuleDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.eclipse.importWizard.EclipseImportBuilder;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultEclipseConfigurator implements EclipseConfigurator {

    @Override
    public void configure(
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final Project project,
        @NotNull final List<EclipseModuleDescriptor> eclipseModules
    ) {
        if (eclipseModules.isEmpty()) {
            return;
        }
        final EclipseImportBuilder eclipseImportBuilder = new EclipseImportBuilder();
        final List<String> projectList = eclipseModules
            .stream()
            .map(AbstractModuleDescriptor::getModuleRootDirectory)
            .map(File::getPath)
            .collect(Collectors.toList());
        if (hybrisProjectDescriptor.getModulesFilesDirectory() != null) {
            eclipseImportBuilder.getParameters().converterOptions.commonModulesDirectory =
                hybrisProjectDescriptor.getModulesFilesDirectory().getPath();
        }
        eclipseImportBuilder.setList(projectList);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            eclipseImportBuilder.commit(project);
        });

    }
}
