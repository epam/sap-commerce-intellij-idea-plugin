/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.impl;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.notifications.Notifications;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.ConsolePersistenceService;
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.RegionPersistenceService;
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.ui.listeners.HybrisConsoleQueryPanelEventManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultConsolePersistenceService implements ConsolePersistenceService {

    private static final Logger LOG = Logger.getInstance(ConsolePersistenceService.class);

    private static final String SOLR = "SOLR";
    private static final String FLEXIBLE_SEARCH = "FLEXIBLE_SEARCH";

    private final Project project;
    private final RegionPersistenceService regionPersistenceService;

    public DefaultConsolePersistenceService(
        final Project project
    ) {
        this.project = project;
        this.regionPersistenceService = RegionPersistenceService.getInstance(project);
    }

    @Override
    public void loadPersistedQueries() {
        if (!HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()) return;

        final var directoryPath = getStoragePath();

        try {
            loadEntitiesFromFile(regionPersistenceService, directoryPath);
            HybrisConsoleQueryPanelEventManager.getInstance(project).notifyListeners();
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Override
    public void persistQueryRegions() {
        final var projectPath = getStoragePath();

        regionPersistenceService.writeRegionData(getRegionPath(projectPath, SOLR), SOLR);
        regionPersistenceService.writeRegionData(getRegionPath(projectPath, FLEXIBLE_SEARCH), FLEXIBLE_SEARCH);
    }

    @NotNull
    private Path getStoragePath() {
        final Path ideaPath;
        var ideModulesFilesDirectory = HybrisProjectSettingsComponent.getInstance(project).getState().getIdeModulesFilesDirectory();
        if (ideModulesFilesDirectory != null) {
            ideaPath = Paths.get(ideModulesFilesDirectory).getParent();
        } else {
            LOG.warn("Cannot properly detect .idea folder for project " + project.getName() + ", falling back to other options.");
            final var projectFile = project.getProjectFile();
            if (projectFile != null) {
                final var projectFileParent = projectFile.getParent();

                ideModulesFilesDirectory = projectFileParent != null
                    ? projectFileParent.getPath()
                    : ProjectUtil.guessProjectDir(project).getPath() + HybrisConstants.EXCLUDE_IDEA_DIRECTORY;
            } else {
                ideModulesFilesDirectory = ProjectUtil.guessProjectDir(project).getPath() + HybrisConstants.EXCLUDE_IDEA_DIRECTORY;
            }
            ideaPath = Paths.get(ideModulesFilesDirectory);
        }

        final var path = ideaPath.resolve(HybrisConstants.QUERY_STORAGE_FOLDER_PATH);

        if (!Files.exists(path)) {
            FileUtil.createDirectory(new File(path.toString()));
        }
        return path;
    }

    private void loadEntitiesFromFile(final RegionPersistenceService loadService, final Path path) throws IOException {
        loadEntityFromFile(loadService, getRegionPath(path, SOLR), SOLR);
        loadEntityFromFile(loadService, getRegionPath(path, FLEXIBLE_SEARCH), FLEXIBLE_SEARCH);
    }

    private void loadEntityFromFile(
        final RegionPersistenceService loadService,
        final Path path,
        final String regionName
    ) throws IOException {
        try {
            loadService.loadRegionData(path, regionName);
        } catch (IllegalArgumentException e) {
            Notifications.create(NotificationType.WARNING,
                    e.getMessage(),
                    HybrisI18NBundleUtils.message("hybris.notification.region.not.allowed")
                )
                .notify(project);
        }
    }

    @NotNull
    private Path getRegionPath(final Path path, final String regionName) {
        return path.resolve(regionName.toLowerCase() + "_region.json");
    }

}