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
package com.intellij.idea.plugin.hybris.acl.vfs.event

import com.intellij.idea.plugin.hybris.acl.editor.AclEditorMarkupModelHelper
import com.intellij.idea.plugin.hybris.acl.psi.AclFile
import com.intellij.idea.plugin.hybris.acl.psi.AclUserRightsValueLineType
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.AsyncFileListener.ChangeApplier
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely

class AclVfsAsyncListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): ChangeApplier? {
        val allEditors = EditorFactory.getInstance().allEditors
        val editors = events
            .mapNotNull { it.file }
            .filter { it.extension == HybrisConstants.ACL_FILE_EXTENSION }
            .distinct()
            .mapNotNull { vf -> allEditors.find { it.virtualFile == vf } }
            .mapNotNull { editor ->
                val project = editor.project ?: return@mapNotNull null
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                    ?.takeIf { it.isPhysical }
                    ?.asSafely<AclFile>()
                    ?: return@mapNotNull null

                editor to psiFile
            }
            .takeIf { it.isNotEmpty() }
            ?.associate { it.second to it.first }
            ?: return null

        return object : ChangeApplier {
            override fun beforeVfsChange() {
                editors.forEach { (psiFile, editor) ->
                    AclEditorMarkupModelHelper.removeHighlighters(editor)

                    PsiTreeUtil.collectElementsOfType(psiFile, AclUserRightsValueLineType::class.java)
                        .forEach { AclEditorMarkupModelHelper.highlightValueLineType(editor, it.textOffset) }
                }
            }
        }
    }
}
