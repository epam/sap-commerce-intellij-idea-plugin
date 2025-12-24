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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.projectImport.ProjectImportBuilder;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.exceptions.HybrisConfigurationException;
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor;
import sap.commerce.toolset.project.localextensions.ProjectLocalExtensionsProcessor;
import sap.commerce.toolset.project.module.ProjectConfigModuleLookup;
import sap.commerce.toolset.project.module.ProjectModulesProcessor;

import java.io.IOException;
import java.util.List;

import static sap.commerce.toolset.HybrisI18nBundle.message;

// TODO: change to coroutine and modal progress
public class SearchModulesRootsTaskModalWindow extends Task.Modal {

    private static final Logger LOG = Logger.getInstance(SearchModulesRootsTaskModalWindow.class);
    protected final HybrisProjectDescriptor hybrisProjectDescriptor;

    public SearchModulesRootsTaskModalWindow(
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor
    ) {
        super(
            ProjectImportBuilder.getCurrentProject(),
            message("hybris.project.import.scanning"),
            true
        );

        this.hybrisProjectDescriptor = hybrisProjectDescriptor;
    }

    @Override
    public void run(@NotNull final ProgressIndicator indicator) {
        try {
            final var moduleDescriptors = ProjectModulesProcessor.Companion.getInstance().process(
                hybrisProjectDescriptor,
                new DirectoriesScannerProgressIndicatorUpdaterProcessor(indicator),
                new DirectoriesScannerErrorsProcessor()
            );

            hybrisProjectDescriptor.setFoundModules(moduleDescriptors);

            final var configModuleDescriptor = ProjectConfigModuleLookup.Companion.getInstance()
                .getConfigModuleDescriptor(hybrisProjectDescriptor);

            ProjectLocalExtensionsProcessor.Companion.getInstance()
                .process(hybrisProjectDescriptor, configModuleDescriptor);
        } catch (final InterruptedException | IOException e) {
            LOG.warn(e);
            hybrisProjectDescriptor.setFoundModules(List.of());
        } catch (final HybrisConfigurationException e) {
            LOG.warn(e);
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showErrorDialog(e.getMessage(), "Project Import");
            });
        }
    }

    @Override
    public void onCancel() {
        this.hybrisProjectDescriptor.clear();
    }
}