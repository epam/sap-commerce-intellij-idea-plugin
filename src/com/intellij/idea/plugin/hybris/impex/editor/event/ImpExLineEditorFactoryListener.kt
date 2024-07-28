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

package com.intellij.idea.plugin.hybris.impex.editor.event

import com.intellij.idea.plugin.hybris.impex.editor.ImpExEditorMarkupModelHelper
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ImpExLineEditorFactoryListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val project = editor.project ?: return

        project.getService(ImpExLineHighlighterService::class.java).highlight(editor)
    }
}

@Service(Service.Level.PROJECT)
private class ImpExLineHighlighterService(private val project: Project, private val coroutineScope: CoroutineScope) {

    fun highlight(editor: Editor) {
        coroutineScope.launch {
            val headerLines = readAction {
                PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                    ?.takeIf { it.isPhysical }
                    ?.asSafely<ImpexFile>()
                    ?.getHeaderLines()
            } ?: return@launch

            headerLines.entries.forEach {
                it.value.forEachIndexed { index, impexValueLine ->
                    ImpExEditorMarkupModelHelper.highlightLine(editor, index, impexValueLine.textOffset)
                }
            }
        }
    }
}