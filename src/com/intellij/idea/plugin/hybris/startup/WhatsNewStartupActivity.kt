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
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.io.StreamUtil
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.jcef.JBCefApp
import java.io.IOException
import java.nio.charset.StandardCharsets

class WhatsNewStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (!ProjectSettingsComponent.getInstance(project).isHybrisProject()) return

        val pluginDescriptor = PluginManager.getInstance().findEnabledPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID))
            ?: return
        val version = pluginDescriptor.version

        RunOnceUtil.runOnceForProject(project, "sapCX_showWhatsNew_$version") {
            try {
                val content = this.javaClass.getResourceAsStream("/CHANGELOG.md").use { html ->
                    html
                        ?.let { String(StreamUtil.readBytes(it), StandardCharsets.UTF_8) }
                } ?: return@runOnceForProject

                val lvf = LightVirtualFile("What's New in SAP Commerce Developers Toolset - $version").also {
                    it.setContent(null, content, true)
                    it.fileType = FileTypeManagerEx.getInstance().getFileTypeByExtension("md")
                    it.isWritable = false
                }

                invokeLater {
                    TextEditorWithPreview.openPreviewForFile(project, lvf)
                }
            } catch (e: IOException) {
                if (!JBCefApp.isSupported()) return@runOnceForProject

                val request = HTMLEditorProvider.Request.url("https://github.com/epam/sap-commerce-intellij-idea-plugin/blob/main/CHANGELOG.md#$version")

                invokeLater {
                    HTMLEditorProvider.openEditor(project, "What's New in SAP Commerce Developers Toolset - $version", request)
                }
            }
        }
    }

}
