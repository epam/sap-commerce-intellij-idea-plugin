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
package com.intellij.idea.plugin.hybris.project.actions

import com.intellij.ide.actions.SearchEverywhereBaseAction
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.type.searcheverywhere.TypeSearchEverywhereContributor
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class GotoTypeAction : SearchEverywhereBaseAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val tabID = TypeSearchEverywhereContributor::class.java.simpleName
        showInSearchEverywherePopup(tabID, e, false, true);
    }

    override fun update(event: AnActionEvent) {
        super.update(event)

        if (event.presentation.isEnabledAndVisible) {
            val project = event.project ?: return
            event.presentation.isEnabledAndVisible = ProjectSettingsComponent.getInstance(project).isHybrisProject()
        }
    }

}
