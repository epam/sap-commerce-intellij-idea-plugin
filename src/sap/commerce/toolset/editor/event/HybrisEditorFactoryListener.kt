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

package sap.commerce.toolset.editor.event

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.util.asSafely
import org.jetbrains.plugins.groovy.GroovyFileType
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.acl.actionSystem.AclFileToolbarInstaller
import sap.commerce.toolset.acl.file.AclFileType
import sap.commerce.toolset.flexibleSearch.actionSystem.FlexibleSearchFileToolbarInstaller
import sap.commerce.toolset.flexibleSearch.file.FlexibleSearchFileType
import sap.commerce.toolset.groovy.actionSystem.GroovyFileToolbarInstaller
import sap.commerce.toolset.impex.actionSystem.ImpExFileToolbarInstaller
import sap.commerce.toolset.impex.file.ImpExFileType
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.polyglotQuery.actionSystem.PolyglotQueryFileToolbarInstaller
import sap.commerce.toolset.polyglotQuery.file.PolyglotQueryFileType

class HybrisEditorFactoryListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val project = event.editor.project ?: return
        val file = event.editor.virtualFile ?: return

        if (SingleRootFileViewProvider.isTooLargeForIntelligence(file)) return
        if (!project.isHybrisProject) return

        val toolbarInstaller = when {
            file.fileType is FlexibleSearchFileType -> FlexibleSearchFileToolbarInstaller.getInstance()
            file.fileType is PolyglotQueryFileType -> PolyglotQueryFileToolbarInstaller.getInstance()
            file.fileType is ImpExFileType -> ImpExFileToolbarInstaller.getInstance()
            file.fileType is AclFileType -> AclFileToolbarInstaller.getInstance()
            Plugin.GROOVY.isActive() && file.fileType is GroovyFileType -> GroovyFileToolbarInstaller.getInstance()
            else -> null
        } ?: return

        event.editor
            .asSafely<EditorEx>()
            ?.takeIf { it.permanentHeaderComponent == null }
            ?.let { toolbarInstaller.toggleToolbar(project, it) }
    }
}