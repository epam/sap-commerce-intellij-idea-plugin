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

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.services.VirtualFileSystemService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.project.utils.Processor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.projectImport.ProjectImportBuilder;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class SearchHybrisDistributionDirectoryTaskModalWindow extends Task.Modal {

    private static final Logger LOG = Logger.getInstance(SearchHybrisDistributionDirectoryTaskModalWindow.class);

    protected final File rootProjectDirectory;
    protected final Processor<String> resultProcessor;

    public SearchHybrisDistributionDirectoryTaskModalWindow(
        @NotNull final File rootProjectDirectory,
        @NotNull final Processor<String> resultProcessor
    ) {
        super(
            ProjectImportBuilder.getCurrentProject(),
            HybrisI18NBundleUtils.message("hybris.project.import.searching.hybris.distribution"),
            true
        );

        Validate.notNull(rootProjectDirectory);
        Validate.notNull(resultProcessor);

        this.rootProjectDirectory = rootProjectDirectory;
        this.resultProcessor = resultProcessor;
    }

    @Override
    public void run(@NotNull final ProgressIndicator indicator) {
        Validate.notNull(indicator);

        final VirtualFileSystemService virtualFileSystemService = ApplicationManager.getApplication().getService(VirtualFileSystemService.class);

        final File hybrisServerShellScriptFile;
        try {
            hybrisServerShellScriptFile = virtualFileSystemService.findFileByNameInDirectory(
                rootProjectDirectory,
                HybrisConstants.HYBRIS_SERVER_SHELL_SCRIPT_NAME,
                new DirectoriesScannerProgressIndicatorUpdaterProcessor(indicator)
            );
        } catch (InterruptedException e) {
            LOG.warn(e);
            return;
        }

        if (null != hybrisServerShellScriptFile
            && null != hybrisServerShellScriptFile.getParentFile()
            && null != hybrisServerShellScriptFile.getParentFile().getParentFile()) {

            this.resultProcessor.process(
                hybrisServerShellScriptFile.getParentFile().getParentFile().getParentFile().getAbsolutePath()
            );
        }
    }

    @Override
    public void onCancel() {
        this.resultProcessor.process("");
    }
}