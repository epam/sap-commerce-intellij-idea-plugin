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
package com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services

import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.pojo.Region
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class RegionPersistenceService(private val project: Project) {

    fun writeRegionData(destination: Path, regionName: String) {
        JsonIOService.getInstance(project).persistData(destination, RegionService.getInstance(project).findOrCreate(regionName))
    }

    fun loadRegionData(source: Path) {
        if (!Files.exists(source)) return
        if (!Files.isRegularFile(source)) return

        JsonIOService.getInstance(project)
            .loadPersistedData(source, Region::class.java)
            ?.let { RegionService.getInstance(project).save(it) }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): RegionPersistenceService = project.getService(RegionPersistenceService::class.java)
    }
}