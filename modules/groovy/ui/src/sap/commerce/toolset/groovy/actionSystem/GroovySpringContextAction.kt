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
import kotlinx.serialization.json.Json
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.groovy.GroovyConstants
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.groovyRemoteSpringBeans
import sap.commerce.toolset.groovy.lang.resolve.RemoteSpringBean
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.i18n
import sap.commerce.toolset.readResource
import sap.commerce.toolset.settings.DeveloperSettings
import sap.commerce.toolset.settings.state.SpringContextMode
import sap.commerce.toolset.settings.state.TransactionMode

abstract class GroovySpringContextAction(private val contextMode: SpringContextMode, description: String) : CheckboxAction(
    contextMode.presentationText, description, null
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean {
        val currentMode = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?.getUserData(GroovyConstants.KEY_SPRING_CONTEXT_MODE)
            ?: e.project?.let { DeveloperSettings.getInstance(it).groovySettings.springContextMode }
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

class GroovyRemoteSpringContextAction : GroovySpringContextAction(
    SpringContextMode.REMOTE,
    i18n("hybris.groovy.actions.springContext.remote.description")
) {
    private var fetching = false

    override fun update(e: AnActionEvent) {
        super.update(e)

        e.presentation.isEnabled = !fetching
        e.presentation.text = if (fetching) "${SpringContextMode.REMOTE.presentationText} | Fetching..."
        else SpringContextMode.REMOTE.presentationText
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val vf = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        vf.putUserData(GroovyConstants.KEY_SPRING_CONTEXT_MODE, SpringContextMode.REMOTE)

        val server = HacExecConnectionService.getInstance(project).activeConnection
        val groovyScript = readResource("scripts/groovy-fetchSpringBeans.groovy")
        val context = GroovyExecContext(
            connection = server,
            executionTitle = "Fetching Spring beans...",
            content = groovyScript,
            transactionMode = TransactionMode.ROLLBACK,
            timeout = server.timeout,
        )

        GroovyExecClient.getInstance(project).execute(
            context,
            beforeCallback = { fetching = true },
            onError = { _, ex ->
                fetching = false
                Notifications
                    .error("Unable to fetch Spring beans", ex.message ?: "")
                    .notify(project)
            }
        ) { it, result ->
            val beans: Collection<RemoteSpringBean>? = result.result
                ?.let { Json.decodeFromString(it) }

            fetching = false
            vf.groovyRemoteSpringBeans = beans

            it.launch {
                if (project.isDisposed) return@launch
                edtWriteAction {
                    PsiDocumentManager.getInstance(project).reparseFiles(listOf(vf), false)
                }
            }

            if (beans == null) {
                Notifications
                    .warning("Unable to fetch Spring beans", result.errorMessage ?: "")
                    .notify(project)
            } else {
                Notifications
                    .info("Found ${beans.size} Spring beans")
                    .notify(project)
            }
        }
    }
}
