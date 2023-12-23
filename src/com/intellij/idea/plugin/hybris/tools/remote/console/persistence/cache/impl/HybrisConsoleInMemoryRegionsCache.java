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

package com.intellij.idea.plugin.hybris.tools.remote.console.persistence.cache.impl;

import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.cache.HybrisConsoleRegionsCache;
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.pojo.Region;

import java.util.HashMap;
import java.util.Map;

public class HybrisConsoleInMemoryRegionsCache implements HybrisConsoleRegionsCache {

    private static final String SOLR = "SOLR";
    private static final String FLEXIBLE_SEARCH = "FLEXIBLE_SEARCH";


    private final Map<String, Region> regions;

    public HybrisConsoleInMemoryRegionsCache() {
        this.regions = new HashMap<>(2);

        regions.put(SOLR, new Region(SOLR, 7));
        regions.put(FLEXIBLE_SEARCH, new Region(FLEXIBLE_SEARCH, 7));
    }

    @Override
    public Map<String, Region> getRegions() {
        return regions;
    }

    @Override
    public Region findRegion(final String regionName) {
        return regions.get(regionName);
    }

}