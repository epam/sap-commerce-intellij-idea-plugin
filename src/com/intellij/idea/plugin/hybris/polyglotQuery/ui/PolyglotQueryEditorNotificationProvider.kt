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
package com.intellij.idea.plugin.hybris.polyglotQuery.ui

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.polyglotQuery.file.PolyglotQueryFileType
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes.*
import com.intellij.idea.plugin.hybris.polyglotQuery.settings.PolyglotQuerySettings
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.ReservedWordsCase
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import java.util.function.Function
import javax.swing.JComponent

class PolyglotQueryEditorNotificationProvider : EditorNotificationProvider, DumbAware {

    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        val settings = HybrisProjectSettingsComponent.getInstance(project)
        if (!settings.isHybrisProject()) return null
        if (!FileTypeRegistry.getInstance().isFileOfType(file, PolyglotQueryFileType.instance)) return null
        val pgqSettings = settings.state.polyglotQuerySettings
        if (!pgqSettings.verifyCaseForReservedWords) return null

        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null

        if (collect(pgqSettings, psiFile).isEmpty()) return null

        return Function { fileEditor ->
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)
            panel.icon(HybrisIcons.HYBRIS)
            panel.text = message(
                "hybris.pgq.notification.provider.keywords.text",
                message("hybris.pgq.notification.provider.keywords.case.${pgqSettings.defaultCaseForReservedWords}")
            )
            panel.createActionLabel(message("hybris.pgq.notification.provider.keywords.action.unify")) {
                WriteCommandAction.runWriteCommandAction(project) {
                    collect(pgqSettings, psiFile).distinct().reversed()
                        .forEach {
                            when (pgqSettings.defaultCaseForReservedWords) {
                                ReservedWordsCase.UPPERCASE -> it.replaceWithText(it.text.uppercase())
                                ReservedWordsCase.LOWERCASE -> it.replaceWithText(it.text.lowercase())
                            }
                        }

                }

                EditorNotifications.getInstance(project).updateNotifications(file)
            }
            panel.createActionLabel(message("hybris.pgq.notification.provider.keywords.action.settings"), "hybris.pgq.openSettings")
            panel
        }
    }

    private fun collect(
        pgqSettings: PolyglotQuerySettings,
        psiFile: PsiFile
    ): MutableCollection<LeafPsiElement> {
        val processor = Collector(pgqSettings)
        PsiTreeUtil.processElements(psiFile, LeafPsiElement::class.java, processor)
        return processor.collection
    }

    class Collector(private val pgqSettings: PolyglotQuerySettings) : PsiElementProcessor.CollectElements<LeafPsiElement>() {
        override fun execute(element: LeafPsiElement): Boolean {
            if (RESERVED_KEYWORDS.contains(element.elementType)) {
                val text = element.text.trim()

                val mismatch = when (pgqSettings.defaultCaseForReservedWords) {
                    ReservedWordsCase.UPPERCASE -> text.contains(REGEX_LOWERCASE)
                    ReservedWordsCase.LOWERCASE -> text.contains(REGEX_UPPERCASE)
                }
                if (mismatch) {
                    return super.execute(element)
                }
            }
            return true
        }
    }

    companion object {
        val REGEX_UPPERCASE = Regex("[A-Z]")
        val REGEX_LOWERCASE = Regex("[a-z]")
        val RESERVED_KEYWORDS = setOf(
            AND,
            ASC,
            BY,
            DESC,
            GET,
            IS,
            NOT,
            NULL,
            OR,
            ORDER,
            WHERE
        )
    }
}
