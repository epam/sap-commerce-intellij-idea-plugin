/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.flexibleSearch.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.flexibleSearch.editor.FlexibleSearchEditorService
import sap.commerce.toolset.flexibleSearch.editor.flexibleSearchSplitEditor
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchPsiFile
import sap.commerce.toolset.i18n
import sap.commerce.toolset.ifNotFromSearchPopup

class FlexibleSearchIntroduceBindParametersAction : DumbAwareAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) = e.ifNotFromSearchPopup {
        e.presentation.text = i18n("hybris.fxs.actions.introduce_bind_parameters")
        e.presentation.description = i18n("hybris.fxs.actions.introduce_bind_parameters.description")
        e.presentation.icon = HybrisIcons.FlexibleSearch.Actions.INTRODUCE_PARAMETERS
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)?.asSafely<FlexibleSearchPsiFile>()
        e.presentation.isEnabledAndVisible = psiFile != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
            ?.asSafely<FlexibleSearchPsiFile>()
            ?: return
        val splitEditor = e.flexibleSearchSplitEditor()
            ?: return

        FlexibleSearchEditorService.getInstance(project).introduceBindParameters(psiFile, splitEditor)
    }
}
