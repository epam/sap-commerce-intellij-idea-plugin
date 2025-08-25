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

package sap.commerce.toolset.console.actionSystem

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.psi.SingleRootFileViewProvider
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.console.HybrisConsole
import sap.commerce.toolset.console.HybrisConsoleService
import sap.commerce.toolset.exec.context.ExecContext
import javax.swing.Icon
import kotlin.reflect.KClass

abstract class OpenInConsoleAction(
    private val fileType: FileType,
    private val consoleClass: KClass<out HybrisConsole<out ExecContext>>,
    private val text: String,
    private val description: String,
    private val icon: Icon? = HybrisIcons.Console.Actions.OPEN
) : DumbAwareAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        e.presentation.text = text
        e.presentation.description = description
        e.presentation.icon = icon
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val content = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
            ?.firstOrNull()
            ?.takeIf { it.fileType == fileType }
            ?.takeUnless { SingleRootFileViewProvider.isTooLargeForIntelligence(it) }
            ?.let { FileDocumentManager.getInstance().getDocument(it) }
            ?.text
            ?: return

        HybrisConsoleService.getInstance(project).openInConsole(consoleClass, content)
    }

}
