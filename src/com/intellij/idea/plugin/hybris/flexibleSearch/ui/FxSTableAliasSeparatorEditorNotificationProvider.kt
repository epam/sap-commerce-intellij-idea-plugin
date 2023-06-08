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
package com.intellij.idea.plugin.hybris.flexibleSearch.ui

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.idea.plugin.hybris.flexibleSearch.settings.FlexibleSearchSettings
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import java.util.function.Function

class FxSTableAliasSeparatorEditorNotificationProvider : AbstractFxSEditorNotificationProvider() {

    override fun shouldCollect(fxsSettings: FlexibleSearchSettings) = fxsSettings.verifyUsedTableAliasSeparator

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
                "hybris.fxs.notification.provider.tableAliasSeparator.text",
                when (val separator = fxsSettings.completion.defaultTableAliasSeparator) {
                    "." -> message("hybris.settings.project.fxs.code.completion.separator.dot")
                    ":" -> message("hybris.settings.project.fxs.code.completion.separator.colon")
                    else -> separator
                }
            )
            panel.createActionLabel(message("hybris.fxs.notification.provider.tableAliasSeparator.action.unify")) {
                WriteCommandAction.runWriteCommandAction(project) {
                    FlexibleSearchElementFactory.createColumnSeparator(project, fxsSettings.completion.defaultTableAliasSeparator)
                        ?.let { newSeparatorLeaf ->
                            collect(fxsSettings, psiFile)
                                .distinct()
                                .reversed()
                                .forEach { it.replace(newSeparatorLeaf) }
                        }
                }

                EditorNotifications.getInstance(project).updateNotifications(file)
            }
            panel.createActionLabel(message("hybris.fxs.notification.provider.tableAliasSeparator.action.settings"), "hybris.fxs.openSettings")
            panel
        }
    }

    override fun collect(
        fxsSettings: FlexibleSearchSettings,
        psiFile: PsiFile
    ): MutableCollection<LeafPsiElement> {
        val searchForElementType = when (fxsSettings.completion.defaultTableAliasSeparator) {
            "." -> COLON
            ":" -> DOT
            else -> return mutableListOf()
        }

        val processor = Collector(searchForElementType)
        PsiTreeUtil.processElements(psiFile, LeafPsiElement::class.java, processor)
        return processor.collection
    }

    class Collector(private val searchForElementType: IElementType) : PsiElementProcessor.CollectElements<LeafPsiElement>() {
        override fun execute(element: LeafPsiElement): Boolean {
            if (element.elementType == searchForElementType && element.parent.parent.elementType == COLUMN_REF_Y_EXPRESSION) {
                return super.execute(element)
            }
            return true
        }
    }

}
