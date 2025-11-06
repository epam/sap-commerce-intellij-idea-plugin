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
package sap.commerce.toolset.groovy.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.CheckboxAction
import com.intellij.openapi.application.edtWriteAction
import com.intellij.psi.PsiDocumentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sap.commerce.toolset.groovy.GroovyConstants
import sap.commerce.toolset.groovy.SpringResolutionMode
import sap.commerce.toolset.i18n

abstract class GroovySpringResolutionAction(text: String, description: String, private val resolutionMode: SpringResolutionMode) : CheckboxAction(
    text, description, null
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        val vf = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
        val currentResolutionMode = e.getData(CommonDataKeys.PSI_FILE)
            ?.getUserData(GroovyConstants.KEY_SPRING_RESOLUTION_MODE)
            ?: SpringResolutionMode.DISABLED

        CoroutineScope(Dispatchers.Default).launch {
            if (project.isDisposed) return@launch

            edtWriteAction {
                PsiDocumentManager.getInstance(project).reparseFiles(listOf(vf), false)
            }
        }

        return currentResolutionMode == resolutionMode
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        psiFile.putUserData(GroovyConstants.KEY_SPRING_RESOLUTION_MODE, resolutionMode)
    }
}

class GroovyDisabledSpringResolutionAction : GroovySpringResolutionAction(
    i18n("hybris.groovy.actions.springResolution.disabled"),
    i18n("hybris.groovy.actions.springResolution.disabled.description"),
    SpringResolutionMode.DISABLED
)

class GroovyLocalSpringResolutionAction : GroovySpringResolutionAction(
    i18n("hybris.groovy.actions.springResolution.local"),
    i18n("hybris.groovy.actions.springResolution.local.description"),
    SpringResolutionMode.LOCAL
)