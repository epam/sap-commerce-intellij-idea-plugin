/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.cache.HybrisConsoleRegionsCache;
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
    private final HybrisConsoleRegionsCache hybrisConsoleRegionsCache;

    public DefaultConsolePersistenceService(
        final Project project
    ) {
        this.project = project;
        this.regionPersistenceService = RegionPersistenceService.getInstance(project);
        this.hybrisConsoleRegionsCache = HybrisConsoleRegionsCache.getInstance(project);
    }

    @Override
    public void loadPersistedQueries() {
        final Path directoryPath = Paths.get(getStoragePath());

        if (Files.exists(directoryPath)) {
            try {
                loadEntitiesFromFile(regionPersistenceService, getStoragePath());
            } catch (IOException e) {
                LOG.error(e);
            }
            HybrisConsoleQueryPanelEventManager.getInstance(project).notifyListeners();
            return;
        }
        FileUtil.createDirectory(new File(String.valueOf(directoryPath)));
        HybrisConsoleQueryPanelEventManager.getInstance(project).notifyListeners();
    }

    @Override
    public void persistQueryRegions() {
        final String projectPath = getStoragePath();
        regionPersistenceService.writeRegionData(getRegionPath(projectPath, SOLR), SOLR);
        regionPersistenceService.writeRegionData(getRegionPath(projectPath, FLEXIBLE_SEARCH), FLEXIBLE_SEARCH);
    }

    @NotNull
    private String getStoragePath() {
        return ProjectUtil.guessProjectDir(project).getPath() + HybrisConstants.QUERY_STORAGE_FOLDER_PATH;
    }

    private void loadEntitiesFromFile(final RegionPersistenceService loadService, final String path) throws IOException {
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
    private Path getRegionPath(final String path, final String regionName) {
        return Paths.get(path + File.separator + regionName.toLowerCase() + "_region.json");
    }

}