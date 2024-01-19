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

package com.intellij.idea.plugin.hybris.project.tasks;

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.projectImport.ProjectImportBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class SearchModulesRootsTaskModalWindow extends Task.Modal {

    protected final File rootProjectDirectory;
    protected final HybrisProjectDescriptor projectImportParameters;

    public SearchModulesRootsTaskModalWindow(
        @NotNull final File rootProjectDirectory,
        @NotNull final HybrisProjectDescriptor projectImportParameters
    ) {
        super(
            ProjectImportBuilder.getCurrentProject(),
            HybrisI18NBundleUtils.message("hybris.project.import.scanning"),
            true
        );

        this.rootProjectDirectory = rootProjectDirectory;
        this.projectImportParameters = projectImportParameters;
    }

    @Override
    public void run(@NotNull final ProgressIndicator indicator) {
        this.projectImportParameters.setRootDirectoryAndScanForModules(
            this.rootProjectDirectory,
            new DirectoriesScannerProgressIndicatorUpdaterProcessor(indicator),
            new DirectoriesScannerErrorsProcessor()
        );
    }

    @Override
    public void onCancel() {
        this.projectImportParameters.clear();
    }
}