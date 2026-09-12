/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.ProjectConstants
import javax.swing.JPanel

/**
 * Shows, next to a declaration, what the running system would actually make of it — which is rarely obvious from the
 * line itself once a chain of files is involved.
 */
class CxPropertiesInlayHintsProvider : InlayHintsProvider<NoSettings> {

    override val key: SettingsKey<NoSettings> = SettingsKey("SapCxProperties")
    override val name: String = "SAP Commerce properties"
    override val group = InlayGroup.VALUES_GROUP
    override val previewText: String? = null
    override val isVisibleInSettings: Boolean = true

    override fun createConfigurable(settings: NoSettings) = object : ImmediateConfigurable {
        override fun createComponent(listener: ChangeListener) = JPanel()
    }

    override fun createSettings(): NoSettings = NoSettings()

    override fun getCollectorFor(file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink) =
        if (file.name in SUPPORTED_FILES && file.isHybrisProject) CxPropertiesInlayHintsCollector(editor)
        else null

    companion object {
        private val SUPPORTED_FILES = setOf(
            ProjectConstants.File.PROJECT_PROPERTIES,
            ProjectConstants.File.LOCAL_PROPERTIES,
        )
    }
}
