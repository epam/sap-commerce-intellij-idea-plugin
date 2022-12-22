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

import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.pojo.Region;
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.RegionPersistenceService;
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.RegionService;
import com.intellij.openapi.project.Project;

import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultJsonRegionPersistenceService implements RegionPersistenceService {

    private final Project project;
    private final RegionService regionService;

    public DefaultJsonRegionPersistenceService(final Project project) {
        this.project = project;
        this.regionService = RegionService.getInstance(project);
    }

    @Override
    public void writeRegionData(final Path destination, final String regionName) {
        JsonIOUtil.getInstance(project).persistData(destination, regionService.findOrCreate(regionName));
    }

    @Override
    public void loadRegionData(final Path source, final String regionName) {
        if (Files.exists(source) && Files.isRegularFile(source)) {
            JsonIOUtil.getInstance(project)
                      .loadPersistedData(source, Region.class)
                      .ifPresent(regionService::save);
        }
    }

}