/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.project.view

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ModuleGroup
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleGroupNode
import com.intellij.openapi.project.Project

class YProjectViewModuleGroupNode(
    project: Project,
    value: ModuleGroup,
    viewSettings: ViewSettings,
) : ProjectViewModuleGroupNode(project, value, viewSettings) {

    override fun update(presentation: PresentationData) {
        super.update(presentation)
        icon?.let { presentation.setIcon(it) }
    }
}