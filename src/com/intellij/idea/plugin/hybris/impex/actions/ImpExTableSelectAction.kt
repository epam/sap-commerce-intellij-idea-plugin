/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRights
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueLine
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.concurrency.AppExecutorUtil

class ImpExTableSelectAction : AbstractImpExTableAction() {

    init {
        with(templatePresentation) {
            text = "Select ImpEx Statement"
            description = "Select ImpEx statement"
            icon = HybrisIcons.TABLE_SELECT
        }
    }

    override fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        ReadAction
            .nonBlocking<TextRange?> {
                return@nonBlocking when (element) {
                    is ImpexUserRights -> element.textRange
                    is ImpexHeaderLine -> element.tableRange
                    is ImpexValueLine -> element.headerLine
                        ?.tableRange
                        ?: return@nonBlocking null

                    else -> return@nonBlocking null
                }
            }
            .finishOnUiThread(ModalityState.defaultModalityState()) {
                editor.selectionModel.setSelection(it.startOffset, it.endOffset)
            }
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    override fun getSuitableElement(element: PsiElement) = PsiTreeUtil
        .getParentOfType(element, ImpexValueLine::class.java, ImpexHeaderLine::class.java, ImpexUserRights::class.java)

    override fun isActionAllowed(project: Project, editor: Editor, element: PsiElement) = getSuitableElement(element) != null

}
