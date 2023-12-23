/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.flexibleSearch.ui

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.settings.FlexibleSearchSettings
import com.intellij.idea.plugin.hybris.settings.ReservedWordsCase
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import java.util.function.Function

class FxSReservedWordsCaseEditorNotificationProvider : AbstractFxSEditorNotificationProvider() {

    override fun shouldCollect(fxsSettings: FlexibleSearchSettings) = fxsSettings.verifyCaseForReservedWords

    override fun panelFunction(
        fxsSettings: FlexibleSearchSettings,
        project: Project,
        psiFile: PsiFile,
        file: VirtualFile
    ): Function<FileEditor, EditorNotificationPanel> {
        return Function { fileEditor ->
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)
            panel.icon(HybrisIcons.Y_LOGO_BLUE)
            panel.text = message(
                "hybris.fxs.notification.provider.keywords.text",
                message("hybris.fxs.notification.provider.keywords.case.${fxsSettings.defaultCaseForReservedWords}")
            )
            panel.createActionLabel(message("hybris.fxs.notification.provider.keywords.action.unify")) {
                WriteCommandAction.runWriteCommandAction(project) {
                    collect(fxsSettings, psiFile).distinct().reversed()
                        .forEach {
                            when (fxsSettings.defaultCaseForReservedWords) {
                                ReservedWordsCase.UPPERCASE -> it.replaceWithText(it.text.uppercase())
                                ReservedWordsCase.LOWERCASE -> it.replaceWithText(it.text.lowercase())
                            }
                        }
                }

                EditorNotifications.getInstance(project).updateNotifications(file)
            }
            panel
        }
    }

    override fun collect(
        fxsSettings: FlexibleSearchSettings,
        psiFile: PsiFile
    ): MutableCollection<LeafPsiElement> {
        val processor = Collector(fxsSettings)
        PsiTreeUtil.processElements(psiFile, LeafPsiElement::class.java, processor)
        return processor.collection
    }

    class Collector(private val fxsSettings: FlexibleSearchSettings) : PsiElementProcessor.CollectElements<LeafPsiElement>() {
        override fun execute(element: LeafPsiElement): Boolean {
            if (HybrisConstants.FXS_RESERVED_KEYWORDS.contains(element.elementType)) {
                val text = element.text.trim()

                val mismatch = when (fxsSettings.defaultCaseForReservedWords) {
                    ReservedWordsCase.UPPERCASE -> text.contains(HybrisConstants.CHARS_LOWERCASE_REGEX)
                    ReservedWordsCase.LOWERCASE -> text.contains(HybrisConstants.CHARS_UPPERCASE_REGEX)
                }
                if (mismatch) {
                    return super.execute(element)
                }
            }
            return true
        }
    }

}
