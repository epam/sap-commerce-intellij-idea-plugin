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
import sap.commerce.toolset.actionSystem.HybrisEditorToolbarProvider
import sap.commerce.toolset.isHybrisProject

class HybrisEditorFactoryListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
            .asSafely<EditorEx>()
            ?.takeIf { it.permanentHeaderComponent == null } ?: return
        val project = editor.project
            ?.takeIf { it.isHybrisProject } ?: return
        val file = editor.virtualFile ?: return

        if (SingleRootFileViewProvider.isTooLargeForIntelligence(file)) return

        HybrisEditorToolbarProvider.EP.extensionList
            .firstOrNull { it.isEnabled(project, file) }
            ?.toggle(project, editor)
    }
}