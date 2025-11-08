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
import sap.commerce.toolset.groovy.SpringContextMode
import sap.commerce.toolset.i18n

abstract class GroovySpringContextAction(private val contextMode: SpringContextMode, description: String) : CheckboxAction(
    contextMode.presentationText, description, null
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean {
        val currentMode = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?.getUserData(GroovyConstants.KEY_SPRING_CONTEXT_MODE)
            ?: SpringContextMode.DISABLED

        return currentMode == contextMode
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val vf = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        vf.putUserData(GroovyConstants.KEY_SPRING_CONTEXT_MODE, contextMode)

        CoroutineScope(Dispatchers.Default).launch {
            if (project.isDisposed) return@launch

            edtWriteAction {
                PsiDocumentManager.getInstance(project).reparseFiles(listOf(vf), false)
            }
        }
    }
}

class GroovyDisabledSpringContextAction : GroovySpringContextAction(
    SpringContextMode.DISABLED,
    i18n("hybris.groovy.actions.springContext.disabled.description")
)

class GroovyLocalSpringContextAction : GroovySpringContextAction(
    SpringContextMode.LOCAL,
    i18n("hybris.groovy.actions.springContext.local.description")
)