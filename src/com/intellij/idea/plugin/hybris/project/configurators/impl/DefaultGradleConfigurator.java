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

import com.intellij.idea.plugin.hybris.project.configurators.GradleConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.GradleModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings.ModuleSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.service.project.open.GradleProjectImportUtil;

import java.util.List;
import java.util.Map;

public class DefaultGradleConfigurator implements GradleConfigurator {

    @Override
    public void configure(
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final Project project,
        @NotNull final List<GradleModuleDescriptor> gradleModules,
        @NotNull final Map<String, String[]> gradleRootGroupMapping
    ) {
        if (gradleModules.isEmpty()) {
            return;
        }

        final var projectDataImportListener = new ProjectDataImportListener() {

            @Override
            public void onImportFinished(final String projectPath) {
                if (projectPath != null && !gradleRootGroupMapping.isEmpty()) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!project.isDisposed()) {
                            final var module = ModuleManager.getInstance(project)
                                                            .findModuleByName(projectPath.substring(projectPath.lastIndexOf('/') + 1));
                            moveGradleModulesToGroup(project, module, gradleRootGroupMapping);
                        }
                    });
                }
            }
        };

        project.getMessageBus().connect().subscribe(
            ProjectDataImportListener.TOPIC,
            projectDataImportListener
        );

        gradleModules.forEach(gradleModule -> GradleProjectImportUtil.linkAndRefreshGradleProject(gradleModule.getGradleFile().getPath(), project));
    }

    private void moveGradleModulesToGroup(
        final Project project,
        final Module gradleModule,
        final Map<String, String[]> gradleRootGroupMapping
    ) {
        if (gradleModule == null) {
            return;
        }
        final var modifiableModuleModel = ModuleManager.getInstance(project).getModifiableModel();

        final ModuleSettings moduleSettings = HybrisProjectSettingsComponent.getInstance(project)
                                                                            .getModuleSettings(gradleModule);
        moduleSettings.setDescriptorType(HybrisModuleDescriptorType.GRADLE.name());
        modifiableModuleModel.setModuleGroupPath(gradleModule, gradleRootGroupMapping.get(gradleModule.getName()));

        ApplicationManager.getApplication().runWriteAction(modifiableModuleModel::commit);
    }
}
