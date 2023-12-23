/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.cache.HybrisConsoleRegionsCache;
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.pojo.Region;
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.RegionService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiFunction;

public class DefaultRegionService implements RegionService {

    private final HybrisConsoleRegionsCache cache;

    public DefaultRegionService(final Project project) {
        this.cache = HybrisConsoleRegionsCache.getInstance(project);
    }

    @Override
    public Region findOrCreate(final String regionName) {
        return cache.getRegions().computeIfAbsent(regionName, Region::new);
    }

    @Override
    public void save(final Region regionFromFile) {
        final Map<String, Region> existingRegions = cache.getRegions();
        final String regionName = regionFromFile.getName();
        if (existingRegions.containsKey(regionName)) {
            existingRegions.compute(regionName, getRegionWithEntitiesBiFunction(regionFromFile));
        } else {
            throw new IllegalArgumentException("UNKNOWN REGION: " + regionFromFile.getName());
        }
    }

    @NotNull
    private BiFunction<String, Region, Region> getRegionWithEntitiesBiFunction(final Region region) {
        return (name, reg) -> {
            reg.getEntities().putAll(region.getEntities());
            return reg;
        };
    }

    @Override
    public void remove(final String name) {
        cache.getRegions().remove(name);
    }

    @Override
    public Map<String, Region> getAll() {
        return cache.getRegions();
    }

    @Override
    public void removeAll() {
        cache.getRegions().clear();
    }
}