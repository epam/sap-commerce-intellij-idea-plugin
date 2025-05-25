/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
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

package com.intellij.idea.plugin.hybris.system.cockpitng.meta

import com.intellij.idea.plugin.hybris.system.meta.MetaModelModificationTracker
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.xml.XmlFile

@Service(Service.Level.PROJECT)
class CngModificationTracker(project: Project) : MetaModelModificationTracker(project) {

    private val stateService = project.service<CngMetaModelStateService>()

    override fun getKeys(vararg xmlFiles: XmlFile): Collection<String>? = xmlFiles
        .mapNotNull { it.virtualFile }
        .map { it.path }

    override fun updateState(keys: Collection<String>) {
        stateService.update(keys)
    }

    companion object {
        val KEY_PROVIDER: (VirtualFile) -> String = { it.path }
    }
}