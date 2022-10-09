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

import com.intellij.ide.actions.ImportModuleAction;
import com.intellij.ide.util.newProjectWizard.AddModuleWizard;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.GradleConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.GradleModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys;
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager;
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.service.project.wizard.GradleProjectImportBuilder;
import org.jetbrains.plugins.gradle.service.project.wizard.GradleProjectImportProvider;

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
        final ProjectDataManager projectDataManager = ApplicationManager.getApplication().getService(ProjectDataManager.class);
        final GradleProjectImportBuilder gradleProjectImportBuilder = new GradleProjectImportBuilder(projectDataManager);
        final GradleProjectImportProvider gradleProjectImportProvider = new GradleProjectImportProvider(
            gradleProjectImportBuilder);

        project.getMessageBus().connect().subscribe(
            ProjectDataImportListener.TOPIC,
            (projectPath) -> {
                if (projectPath != null && !gradleRootGroupMapping.isEmpty()) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!project.isDisposed()) {
                            final Module module = ModuleManager.getInstance(project).findModuleByName(projectPath.substring(projectPath.lastIndexOf('/') + 1));
                            moveGradleModulesToGroup(project, module, gradleRootGroupMapping);
                        }
                    });
                }
            }
        );
        gradleModules.forEach(gradleModule -> {
            ApplicationManager.getApplication().invokeAndWait(() -> {
                final AddModuleWizard wizard = new AddModuleWizard(
                    project,
                    gradleModule.getGradleFile().getPath(),
                    gradleProjectImportProvider
                );
                if (wizard.getStepCount() > 0) {
                    final ModuleWizardStep step = wizard.getCurrentStepObject();
                    if (step.isStepVisible()) {
                        step.updateStep();
                        step.updateDataModel();
                    }
                }
                wizard.doFinishAction();
                ImportModuleAction.createFromWizard(project, wizard);
            });
        });

        project.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, Boolean.TRUE);
    }

    private void moveGradleModulesToGroup(
        final Project project,
        final Module gradleModule,
        final Map<String, String[]> gradleRootGroupMapping
    ) {
        if (gradleModule == null) {
            return;
        }
        final ModifiableModuleModel modifiableModuleModel = ModuleManager.getInstance(project).getModifiableModel();

        gradleModule.setOption(HybrisConstants.DESCRIPTOR_TYPE, HybrisModuleDescriptorType.GRADLE.name());
        modifiableModuleModel.setModuleGroupPath(gradleModule, gradleRootGroupMapping.get(gradleModule.getName()));

        ApplicationManager.getApplication().runWriteAction(modifiableModuleModel::commit);
    }
}
