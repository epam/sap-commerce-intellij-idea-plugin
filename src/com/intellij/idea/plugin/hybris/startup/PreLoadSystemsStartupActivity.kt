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
package com.intellij.idea.plugin.hybris.startup

import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class PreLoadSystemsStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (!CommonIdeaService.getInstance().isHybrisProject(project)) {
            return
        }

        try {
            TSMetaModelAccess.getInstance(project).getMetaModel()
        } catch (e: ProcessCanceledException) {
            // ignore
        }

        try {
            BSMetaModelAccess.getInstance(project).getMetaModel()
        } catch (e: ProcessCanceledException) {
            // ignore
        }

        try {
            CngMetaModelAccess.getInstance(project).getMetaModel()
        } catch (e: ProcessCanceledException) {
            // ignore
        }
    }
}