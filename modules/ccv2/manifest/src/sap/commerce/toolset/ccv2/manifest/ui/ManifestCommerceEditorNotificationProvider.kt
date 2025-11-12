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

package sap.commerce.toolset.ccv2.manifest.ui

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.manifest.jsonSchema.providers.ManifestCommerceJsonSchemaFileProvider
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import java.util.function.Function
import javax.swing.JComponent

class ManifestCommerceEditorNotificationProvider : EditorNotificationProvider, DumbAware {

    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (!ManifestCommerceJsonSchemaFileProvider.getInstance(project).isAvailable(file)) return null

        val subscriptions = CCv2ProjectSettings.getInstance().subscriptions

        if (subscriptions.isNotEmpty()) return null

        return Function { fileEditor ->
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)
            panel.icon(HybrisIcons.Y.LOGO_BLUE)
            panel.text = "Unleash the power of CI/CD with rich, built-in, and fully manageable CCv2 capabilities in your IDE."
            panel.createActionLabel("CCv2 settings", "ccv2.open.settings.action")
            panel
        }
    }

}