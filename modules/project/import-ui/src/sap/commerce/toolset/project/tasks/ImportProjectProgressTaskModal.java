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

package sap.commerce.toolset.project.tasks;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator;
import sap.commerce.toolset.project.configurator.ModuleProvider;
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator;
import sap.commerce.toolset.project.configurator.ProjectPreImportConfigurator;
import sap.commerce.toolset.project.context.ProjectImportContext;

import java.util.List;
import java.util.Optional;

import static sap.commerce.toolset.HybrisI18nBundle.message;

// TODO: change to coroutine and modal progress
public class ImportProjectProgressTaskModal extends Task.Modal {

    private final Project project;
    private final ProjectImportContext importContext;
    private final List<Module> modules;

    public ImportProjectProgressTaskModal(
        final Project project,
        final ProjectImportContext importContext,
        final List<Module> modules
    ) {
        super(project, message("hybris.project.import.commit"), false);
        this.project = project;
        this.importContext = importContext;
        this.modules = modules;
    }

    @Override
    public synchronized void run(@NotNull final ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        indicator.setText(message("hybris.project.import.preparation"));

        final var modifiableModelsProvider = new IdeModifiableModelsProviderImpl(project);

        ProjectPreImportConfigurator.Companion.getEP().getExtensionList().forEach(configurator -> {
                indicator.setText("Pre-configuring project using '%s' Configurator...".formatted(configurator.getName()));
                configurator.preConfigure(importContext);
            }
        );

        indicator.setIndeterminate(false);
        indicator.setFraction(0d);

        final var chosenModuleDescriptors = importContext.getAllChosenModuleDescriptors();
        final var moduleProviders = ModuleProvider.Companion.getEP().getExtensionList();
        final var moduleImportConfigurators = ModuleImportConfigurator.Companion.getEP().getExtensionList();

        chosenModuleDescriptors.stream()
            .map(moduleDescriptor ->
                moduleProviders.stream()
                    .filter(provider -> provider.isApplicable(moduleDescriptor))
                    .findFirst()
                    .map(provider -> {
                            indicator.setText("Configuring '%s' (%s) module...".formatted(moduleDescriptor.getName(), provider.getName()));

                            final var moduleTypeId = provider.getModuleTypeId();
                            final var module = provider.create(importContext, moduleDescriptor, modifiableModelsProvider);

                            moduleImportConfigurators.stream()
                                .filter(configurator -> configurator.isApplicable(moduleTypeId))
                                .forEach(configurator -> {
                                    indicator.setText2("Configuring module using '%s'".formatted(configurator.getName()));

                                    configurator.configure(importContext, moduleDescriptor, module, modifiableModelsProvider);
                                });

                            return module;
                        }
                    )
            )
            .flatMap(Optional::stream)
            .forEach(module -> {
                modules.add(module);
                indicator.setFraction((double) modules.size() / chosenModuleDescriptors.size());
            });
        indicator.setText2(null);
        indicator.setIndeterminate(true);

        ProjectImportConfigurator.Companion.getEP().getExtensionList().forEach(configurator -> {
                indicator.setText("Configuring project using '%s' Configurator...".formatted(configurator.getName()));
                configurator.configure(importContext, modifiableModelsProvider);
            }
        );

        indicator.setText(message("hybris.project.import.saving.project"));

        ApplicationManager.getApplication()
            .invokeAndWait(() -> ApplicationManager.getApplication()
                .runWriteAction(modifiableModelsProvider::commit)
            );

        project.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, Boolean.TRUE);
    }
}
