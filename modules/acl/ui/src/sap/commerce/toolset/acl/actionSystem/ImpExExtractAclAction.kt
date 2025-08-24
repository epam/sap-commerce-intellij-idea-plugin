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

package sap.commerce.toolset.acl.actionSystem

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.EditorNotifications
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.acl.AclLanguage
import sap.commerce.toolset.acl.file.AclFileType
import sap.commerce.toolset.impex.psi.ImpExFile
import sap.commerce.toolset.impex.psi.ImpExUserRights

class ImpExExtractAclAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val impExFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
            ?.asSafely<ImpExFile>()
            ?: return

        CoroutineScope(Dispatchers.EDT).launch {
            val userRights = withContext(Dispatchers.Default) {
                readAction { PsiTreeUtil.collectElementsOfType(psiFile, ImpExUserRights::class.java) }
            }

            val directory = PsiManager.getInstance(project).findDirectory(impExFile.parent) ?: return@launch
            val aclFileName = impExFile.nameWithoutExtension + "." + AclFileType.defaultExtension
            val aclExistingFile = directory.findFile(aclFileName)
            var aclShouldReplace = false

            if (aclExistingFile != null) {
                val choice = Messages.showYesNoDialog(
                    project,
                    "File '${aclExistingFile.name}' already exists. Do you want to replace its content?",
                    "File Already Exists",
                    "Replace",
                    "Cancel",
                    Messages.getQuestionIcon()
                )
                aclShouldReplace = (choice == Messages.YES)
            }

            if (aclShouldReplace || aclExistingFile == null) {
                val aclContent = readAction {
                    userRights
                        .filter { it.isValid }
                        .joinToString("\n\n") { it.text }
                }

                WriteCommandAction.runWriteCommandAction(project) {
                    val psiDocumentManager = PsiDocumentManager.getInstance(project)

                    val aclTargetFile = aclExistingFile
                        ?: directory.createFile(aclFileName)

                    psiDocumentManager.getDocument(aclTargetFile)
                        ?.apply {
                            val aclContentFormatted = PsiFileFactory.getInstance(project).createFileFromText(AclLanguage, aclContent)
                                .let { CodeStyleManager.getInstance(project).reformat(it) }
                                .text

                            setText(aclContentFormatted)
                            psiDocumentManager.commitDocument(this)
                        }

                    userRights
                        .reversed()
                        .filter { it.isValid }
                        .forEach { it.delete() }

                    aclTargetFile.virtualFile?.let { aclVirtualFile ->
                        FileEditorManager.getInstance(project).openFile(aclVirtualFile, true)
                    }
                }

                EditorNotifications.getInstance(project).updateNotifications(impExFile)
            }
        }
    }
}