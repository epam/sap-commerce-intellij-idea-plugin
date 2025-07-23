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

package com.intellij.idea.plugin.hybris.editor

import com.intellij.idea.plugin.hybris.acl.editor.AclSplitEditor
import com.intellij.idea.plugin.hybris.acl.file.AclFileType
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchSplitEditor
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFileType
import com.intellij.idea.plugin.hybris.groovy.editor.GroovySplitEditor
import com.intellij.idea.plugin.hybris.impex.editor.ImpExSplitEditor
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.idea.plugin.hybris.polyglotQuery.editor.PolyglotQuerySplitEditor
import com.intellij.idea.plugin.hybris.polyglotQuery.file.PolyglotQueryFileType
import com.intellij.idea.plugin.hybris.project.utils.Plugin
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.asSafely
import org.jetbrains.plugins.groovy.GroovyFileType

class HybrisSplitFileEditorProvider : FileEditorProvider, DumbAware {

    override fun createEditor(project: Project, file: VirtualFile): FileEditor = with(TextEditorProvider.Companion.getInstance().createEditor(project, file)) {
        asSafely<TextEditor>()
            ?.let {
                when (file.fileType) {
                    is FlexibleSearchFileType -> FlexibleSearchSplitEditor(it, project)
                    is PolyglotQueryFileType -> PolyglotQuerySplitEditor(it, project)
                    is ImpexFileType -> ImpExSplitEditor(it, project)
                    is AclFileType -> AclSplitEditor(it, project)
                    else -> if (Plugin.GROOVY.isActive() && file.fileType is GroovyFileType) GroovySplitEditor(it, project)
                    else null
                }
            }
            ?: this
    }

    override fun getEditorTypeId(): String = "hybris-split-file-editor"
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
    override fun accept(project: Project, file: VirtualFile): Boolean = file.fileType is FlexibleSearchFileType
        || file.fileType is PolyglotQueryFileType
        || file.fileType is ImpexFileType
        || file.fileType is AclFileType
        || (Plugin.GROOVY.isActive() && file.fileType is GroovyFileType)
}