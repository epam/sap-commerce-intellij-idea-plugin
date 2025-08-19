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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.projectImport.ProjectImportBuilder;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.project.utils.Processor;
import sap.commerce.toolset.project.vfs.VirtualFileSystemService;

import java.io.File;

import static sap.commerce.toolset.HybrisI18NBundleUtils.message;

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
            message("hybris.project.import.searching.hybris.distribution"),
            true
        );

        this.rootProjectDirectory = rootProjectDirectory;
        this.resultProcessor = resultProcessor;
    }

    @Override
    public void run(@NotNull final ProgressIndicator indicator) {
        final var virtualFileSystemService = VirtualFileSystemService.getInstance();

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