/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.configurators.impl;

import sap.commerce.toolset.project.configurators.EclipseConfigurator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.eclipse.importWizard.EclipseImportBuilder;
import sap.commerce.toolset.project.descriptors.HybrisProjectDescriptor;
import sap.commerce.toolset.project.descriptors.impl.AbstractModuleDescriptor;
import sap.commerce.toolset.project.descriptors.impl.EclipseModuleDescriptor;

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
