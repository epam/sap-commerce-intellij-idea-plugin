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
import sap.commerce.toolset.project.configurator.ModuleFacetConfigurator;
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator;
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator;
import sap.commerce.toolset.project.configurator.ProjectPreImportConfigurator;
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor;

import java.util.List;
import java.util.Optional;

import static sap.commerce.toolset.HybrisI18NBundleUtils.message;

public class ImportProjectProgressModalWindow extends Task.Modal {

    private final Project project;
    private final HybrisProjectDescriptor hybrisProjectDescriptor;
    private final List<Module> modules;

    public ImportProjectProgressModalWindow(
        final Project project,
        final HybrisProjectDescriptor hybrisProjectDescriptor,
        final List<Module> modules
    ) {
        super(project, message("hybris.project.import.commit"), false);
        this.project = project;
        this.hybrisProjectDescriptor = hybrisProjectDescriptor;
        this.modules = modules;
    }

    @Override
    public synchronized void run(@NotNull final ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        indicator.setText(message("hybris.project.import.preparation"));

        final var modifiableModelsProvider = new IdeModifiableModelsProviderImpl(project);
        final var rootProjectModifiableModel = modifiableModelsProvider.getModifiableModuleModel();

        ProjectPreImportConfigurator.Companion.getEP().getExtensionList().forEach(configurator -> {
                indicator.setText("Configuring project using '%s' Configurator...".formatted(configurator.getName()));
                configurator.preConfigure(hybrisProjectDescriptor);
            }
        );

        indicator.setIndeterminate(false);
        indicator.setFraction(0d);

        final var chosenModuleDescriptors = hybrisProjectDescriptor.getChosenModuleDescriptors();
        final var moduleImportConfigurators = ModuleImportConfigurator.Companion.getEP().getExtensionList();
        final var moduleFacetConfigurators = ModuleFacetConfigurator.Companion.getEP().getExtensionList();

        chosenModuleDescriptors.stream()
            .map(moduleDescriptor ->
                moduleImportConfigurators.stream()
                    .filter(configurator -> configurator.isApplicable(moduleDescriptor))
                    .findFirst()
                    .map(configurator -> {
                            indicator.setText("Configuring project using '%s' Configurator...".formatted(configurator.getName()));
                            indicator.setText2("Configuring module: %s".formatted(moduleDescriptor.getName()));

                            final var module = configurator.configure(hybrisProjectDescriptor, moduleDescriptor, modifiableModelsProvider, rootProjectModifiableModel);

                            indicator.setText2("Configuring facets for module: %s".formatted(moduleDescriptor.getName()));
                            final var modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(module);
                            final var modifiableFacetModel = modifiableModelsProvider.getModifiableFacetModel(module);

                            moduleFacetConfigurators.forEach(facetConfigurator ->
                                facetConfigurator.configureModuleFacet(module, hybrisProjectDescriptor, modifiableFacetModel, moduleDescriptor, modifiableRootModel)
                            );
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
                configurator.configure(hybrisProjectDescriptor, modifiableModelsProvider);
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
