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
package com.intellij.idea.plugin.hybris.tools.remote.console.persistence.pojo

import java.io.Serial

class RegionEntityFIFOCache<T>(private val maxNumberEntities: Int) : LinkedHashMap<String, RegionEntity<T>>() {

    // required for deserialization
    constructor() : this(-1)

    override fun removeEldestEntry(eldest: Map.Entry<String, RegionEntity<T>>) = if (maxNumberEntities < 0) false
    else this.size > maxNumberEntities

    companion object {
        @Serial
        private const val serialVersionUID = 2873734604163564844L
    }
}