/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.toolwindow.CopyFileToHybrisConsoleDialog
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

object CopyFileToHybrisConsoleUtils {

    fun copySelectedFilesToConsole(project: Project, consoleTitle: String, dialogTitle: String) {
        val console = HybrisToolWindowService.getInstance(project).consolesPanel.findConsole(consoleTitle) ?: return
        val query = getQueryFromSelectedFiles(project)

        if (getTextFromHybrisConsole(project, console).isNotEmpty()) {
            CopyFileToHybrisConsoleDialog(project, getDialogTitleFromProperties(dialogTitle))
                .show { copyQueryToConsole(project, consoleTitle, query) }
        } else {
            copyQueryToConsole(project, consoleTitle, query)
        }
    }

    fun copyQueryToConsole(project: Project, consoleTitle: String, query: String) {
        val panel = HybrisToolWindowService.getInstance(project).consolesPanel
        val console = panel.findConsole(consoleTitle) ?: return

        with(HybrisToolWindowService.getInstance(project)) {
            this.activateToolWindow()
            this.activateToolWindowTab(HybrisToolWindowFactory.CONSOLES_ID)
        }

        with(console) {
            panel.setActiveConsole(this)
            this.clear()
            this.setInputText(query)
        }
    }

    fun isRequiredSingleFileExtension(project: Project, fileExtension: String) = getFileExtensions(project)
        .takeIf { it.size == 1 }
        ?.any { it == fileExtension }
        ?: false

    fun isRequiredMultipleFileExtension(project: Project, fileExtension: String) = getFileExtensions(project)
        .takeUnless { it.isEmpty() }
        ?.all { it == fileExtension }
        ?: false

    private fun getTextFromHybrisConsole(project: Project, console: HybrisConsole): String {
        val helper = LanguageConsoleImpl.Helper(project, console.virtualFile)
        val consoleExecutionEditor = ConsoleExecutionEditor(helper)
        val text = consoleExecutionEditor.document.text
        Disposer.dispose(consoleExecutionEditor)

        return text
    }

    private fun getFileExtensions(project: Project) = getSelectedFiles(project)
        .mapNotNull { it.extension }

    private fun getQueryFromSelectedFiles(project: Project) = getSelectedFiles(project)
        .mapNotNull { getPsiFileNode(project, it) }
        .joinToString(System.lineSeparator()) { it.text }

    private fun getPsiFileNode(project: Project, virtualFile: VirtualFile) = PsiManager.getInstance(project)
        .findFile(virtualFile)

    private fun getSelectedFiles(project: Project) = getSelectedTreePaths(project)
        ?.mapNotNull { getVirtualFile(it) }
        ?: emptyList()

    private fun getVirtualFile(treePath: TreePath) = treePath.lastPathComponent
        ?.let { it as? DefaultMutableTreeNode }
        ?.userObject
        ?.let { it as? ProjectViewNode<*> }
        ?.virtualFile
        ?.takeUnless { it.isDirectory }

    private fun getSelectedTreePaths(project: Project) = ProjectView.getInstance(project)
        .currentProjectViewPane
        .selectionPaths

    private fun getDialogTitleFromProperties(fileExtension: String) = messageFallback(HybrisConstants.DIALOG_TITLE + fileExtension, fileExtension)
}