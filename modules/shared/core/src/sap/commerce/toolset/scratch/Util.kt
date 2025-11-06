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

package sap.commerce.toolset.scratch

import com.intellij.ide.scratch.ScratchRootType
import com.intellij.lang.Language
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun createScratchFile(
    project: Project,
    text: String,
    language: Language
) {
    CoroutineScope(Dispatchers.Default).launch {
        val scratchRoot = ScratchRootType.getInstance()
        val fileType = FileTypeRegistry.getInstance().findFileTypeByLanguage(language)
            ?: return@launch
        val fileName = "scratch.${fileType.defaultExtension}"

        val vf = edtWriteAction { scratchRoot.createScratchFile(project, fileName, language, text) }
            ?: return@launch

        withContext(Dispatchers.EDT) {
            FileEditorManager.getInstance(project).openFile(vf, true)
        }
    }
}

fun createScratchFile(
    project: Project,
    text: String,
    fileExtension: String
) {
    CoroutineScope(Dispatchers.Default).launch {
        val scratchRoot = ScratchRootType.getInstance()
        val fileType = FileTypeRegistry.getInstance().getFileTypeByExtension(fileExtension)
            .asSafely<LanguageFileType>()
            ?: return@launch
        val fileName = "scratch.${fileType.defaultExtension}"
        val language = fileType.language

        createAndOpenScratchFile(scratchRoot, project, fileName, language, text)
    }
}

private suspend fun createAndOpenScratchFile(
    scratchRoot: ScratchRootType,
    project: Project,
    fileName: String,
    language: Language,
    text: String
) {
    val vf = edtWriteAction { scratchRoot.createScratchFile(project, fileName, language, text) }
        ?: return

    withContext(Dispatchers.EDT) {
        FileEditorManager.getInstance(project).openFile(vf, true)
    }
}
