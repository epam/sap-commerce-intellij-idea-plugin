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
package com.intellij.idea.plugin.hybris.actions

import com.intellij.execution.console.ConsoleExecutionEditor
import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.messageFallback
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionContext
import com.intellij.idea.plugin.hybris.toolwindow.OpenInConsoleConsoleDialog
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.util.asSafely
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import kotlin.reflect.KClass

@Service(Service.Level.PROJECT)
class OpenInHybrisConsoleService(private val project: Project) {

    fun openSelectedFilesInConsole(consoleClass: KClass<out HybrisConsole<out ExecutionContext>>, fileExtension: String) {
        val console = HybrisConsoleService.getInstance(project).openConsole(consoleClass) ?: return
        val content = getContentFromSelectedFiles()

        if (getTextFromHybrisConsole(console).isNotEmpty()) {
            OpenInConsoleConsoleDialog(project, getDialogTitleFromProperties(fileExtension))
                .show { openInConsole(consoleClass, content) }
        } else {
            openInConsole(consoleClass, content)
        }
    }

    fun openInConsole(consoleClass: KClass<out HybrisConsole<out ExecutionContext>>, content: String) {
        val console = HybrisConsoleService.getInstance(project).openConsole(consoleClass) ?: return

        console.clear()
        console.setInputText(content)
    }

    fun isRequiredSingleFileExtension(fileExtension: String) = getFileExtensions()
        .takeIf { it.size == 1 }
        ?.any { it == fileExtension }
        ?: false

    fun isRequiredMultipleFileExtension(fileExtension: String) = getFileExtensions()
        .takeUnless { it.isEmpty() }
        ?.all { it == fileExtension }
        ?: false

    private fun getTextFromHybrisConsole(console: HybrisConsole<out ExecutionContext>): String {
        val helper = LanguageConsoleImpl.Helper(project, console.virtualFile)
        val consoleExecutionEditor = ConsoleExecutionEditor(helper)
        val text = consoleExecutionEditor.document.text
        Disposer.dispose(consoleExecutionEditor)

        return text
    }

    private fun getFileExtensions() = getSelectedFiles()
        .mapNotNull { it.extension }

    private fun getContentFromSelectedFiles() = getSelectedFiles()
        .mapNotNull { getPsiFileNode(it) }
        .joinToString(System.lineSeparator()) { it.text }

    private fun getPsiFileNode(virtualFile: VirtualFile) = PsiManager.getInstance(project)
        .findFile(virtualFile)

    private fun getSelectedFiles() = getSelectedTreePaths()
        ?.mapNotNull { getVirtualFile(it) }
        ?: emptyList()

    private fun getVirtualFile(treePath: TreePath) = treePath.lastPathComponent
        ?.asSafely<DefaultMutableTreeNode>()
        ?.userObject
        ?.asSafely<ProjectViewNode<*>>()
        ?.virtualFile
        ?.takeUnless { it.isDirectory }

    private fun getSelectedTreePaths() = ProjectView.getInstance(project)
        .currentProjectViewPane
        ?.selectionPaths

    private fun getDialogTitleFromProperties(fileExtension: String) = messageFallback(HybrisConstants.DIALOG_TITLE + fileExtension, fileExtension)

    companion object {
        fun getInstance(project: Project): OpenInHybrisConsoleService = project.service()
    }
}