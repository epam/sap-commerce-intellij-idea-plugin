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
package sap.commerce.toolset.startup

import sap.commerce.toolset.system.bean.meta.BSMetaModelStateService
import sap.commerce.toolset.system.cockpitng.meta.CngMetaModelStateService
import sap.commerce.toolset.system.spring.SimpleSpringService
import sap.commerce.toolset.system.type.meta.TSMetaModelStateService
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.project.PropertyService

class PreLoadSystemsStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (project.isNotHybrisProject) return

        refreshSystem(project) { TSMetaModelStateService.getInstance(project).init() }
        refreshSystem(project) { BSMetaModelStateService.getInstance(project).init() }
        refreshSystem(project) { CngMetaModelStateService.getInstance(project).init() }

        SimpleSpringService.getService(project)
            ?.let { service -> refreshSystem(project) { service.initCache() } }
        PropertyService.getInstance(project)
            .let { service -> refreshSystem(project) { service.initCache() } }
    }

    private fun refreshSystem(project: Project, refresher: (Project) -> Unit) {
        DumbService.getInstance(project).runWhenSmart {
            try {
                refresher.invoke(project)
            } catch (_: Throwable) {
                // ignore
            }
        }
    }
}