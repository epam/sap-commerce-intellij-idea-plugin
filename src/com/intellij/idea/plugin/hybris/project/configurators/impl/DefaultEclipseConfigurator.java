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
import com.intellij.idea.plugin.hybris.project.descriptors.AbstractHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.EclipseModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.eclipse.importWizard.EclipseImportBuilder;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/11/16.
 */
public class DefaultEclipseConfigurator implements EclipseConfigurator {

    @Override
    public void configure(
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final Project project,
        @NotNull final List<EclipseModuleDescriptor> eclipseModules,
        @NotNull final Map<String,String[]> eclipseGroupMapping
    ) {
        if (eclipseModules.isEmpty()) {
            return;
        }
        final EclipseImportBuilder eclipseImportBuilder = new EclipseImportBuilder();
        final List<String> projectList = eclipseModules
            .stream()
            .map(AbstractHybrisModuleDescriptor::getRootDirectory)
            .map(File::getPath)
            .collect(Collectors.toList());
        if (hybrisProjectDescriptor.getModulesFilesDirectory() != null) {
            eclipseImportBuilder.getParameters().converterOptions.commonModulesDirectory =
                hybrisProjectDescriptor.getModulesFilesDirectory().getPath();
        }
        eclipseImportBuilder.setList(projectList);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            final List<Module> newRootModules = eclipseImportBuilder.commit(project);
            moveEclipseModulesToGroup(project, newRootModules, eclipseGroupMapping);
        });

    }

    private void moveEclipseModulesToGroup(
        @NotNull final Project project,
        @NotNull final List<Module> eclipseModules,
        @NotNull final Map<String,String[]> eclipseGroupMapping
    ) {
        final var modifiableModuleModel = ModuleManager.getInstance(project).getModifiableModel();
        final var settingsComponent = HybrisProjectSettingsComponent.getInstance(project);

        for (final Module module : eclipseModules) {
            settingsComponent.getModuleSettings(module).setDescriptorType(HybrisModuleDescriptorType.ECLIPSE.name());
            modifiableModuleModel.setModuleGroupPath(module, eclipseGroupMapping.get(module.getName()));
        }

        ApplicationManager.getApplication().runWriteAction(modifiableModuleModel::commit);
    }
}
