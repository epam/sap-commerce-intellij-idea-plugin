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

package com.intellij.idea.plugin.hybris.tools.remote.console.persistence.pojo;

public class Region<T> {

    private String name;
    private RegionEntityFIFOCache<T> entities;
    private int maxNumberEntities;

    public Region() {
    }

    public Region(final String name) {
        this.maxNumberEntities = -1;
        this.entities = new RegionEntityFIFOCache<>(maxNumberEntities);
        this.name = name;
    }

    public Region(final String name, final int maxNumberEntities) {
        this(name);
        this.maxNumberEntities = maxNumberEntities;
        this.entities = new RegionEntityFIFOCache<>(maxNumberEntities);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public RegionEntityFIFOCache<T> getEntities() {
        return entities;
    }

    public void setEntities(final RegionEntityFIFOCache<T> entities) {
        this.entities = entities;
    }

    public int getMaxNumberEntities() {
        return maxNumberEntities;
    }

    public void setMaxNumberEntities(final int maxNumberEntities) {
        this.maxNumberEntities = maxNumberEntities;
    }

}