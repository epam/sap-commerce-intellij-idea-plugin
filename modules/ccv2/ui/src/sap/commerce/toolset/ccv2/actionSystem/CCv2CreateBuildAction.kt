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

package sap.commerce.toolset.ccv2.actionSystem

import com.intellij.openapi.actionSystem.AnActionEvent
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab
import sap.commerce.toolset.ccv2.ui.CCv2CreateBuildDialog

class CCv2CreateBuildAction : CCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Schedule Build",
    icon = HybrisIcons.CCv2.Build.Actions.CREATE
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val subscription = CCv2DeveloperSettings.getInstance(project).getActiveCCv2Subscription()

        CCv2CreateBuildDialog(project, subscription).showAndGet()
    }
}