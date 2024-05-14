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
package com.intellij.idea.plugin.hybris.startup

import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.util.RunOnceUtil
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.ui.jcef.JBCefApp

class WhatsNewStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (!ProjectSettingsComponent.getInstance(project).isHybrisProject()) return
        if (!JBCefApp.isSupported()) return

        val pluginDescriptor = PluginManager.getInstance().findEnabledPlugin(PluginId.getId("com.intellij.idea.plugin.sap.commerce"))
            ?: return
        val version = pluginDescriptor.version

        RunOnceUtil.runOnceForProject(project, "sapCX_showWhatsNew_$version") {
            val request = HTMLEditorProvider.Request.url("https://github.com/epam/sap-commerce-intellij-idea-plugin/blob/main/CHANGELOG.md#$version")

            invokeLater {
                HTMLEditorProvider.openEditor(project, "See What's New", request)
            }
        }
    }
}
