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

package sap.commerce.toolset.groovy.exec

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext.Settings
import sap.commerce.toolset.groovy.exec.context.GroovyReplicaAwareContext
import sap.commerce.toolset.groovy.getSpringContextMode
import sap.commerce.toolset.groovy.groovyRemoteSpringBeans
import sap.commerce.toolset.groovy.groovySettings
import sap.commerce.toolset.groovy.lang.resolve.RemoteSpringBean
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.readResource
import sap.commerce.toolset.settings.state.SpringContextMode
import sap.commerce.toolset.settings.state.TransactionMode
import sap.commerce.toolset.settings.yDeveloperSettings

@Service(Service.Level.PROJECT)
class GroovyExecService(private val project: Project) {

    private var fetching = false

    fun fetchRemoteSpringBeans(virtualFile: VirtualFile) {
        val webContext = getActiveWebContext(virtualFile)

        val server = HacExecConnectionService.getInstance(project).activeConnection
        val groovyScript = readResource("scripts/groovy-fetchSpringBeans.groovy")
        val context = GroovyExecContext(
            connection = server,
            executionTitle = "Fetching Spring beans for '$webContext' web context...",
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
                    .warning(
                        "Unable to fetch Spring beans",
                        """
                            Web context: $webContext<br>
                            ${ex.message ?: ""}
                        """.trimIndent()
                    )
                    .notify(project)
            }
        ) { it, result ->
            val beans: Collection<RemoteSpringBean>? = result.result
                ?.let { Json.decodeFromString(it) }

            fetching = false
            virtualFile.groovyRemoteSpringBeans = beans

            it.launch {
                if (project.isDisposed) return@launch
                edtWriteAction {
                    PsiDocumentManager.getInstance(project).reparseFiles(listOf(virtualFile), false)
                }
            }

            if (beans == null) {
                Notifications
                    .warning(
                        "Unable to fetch Spring beans",
                        """
                            Web context: $webContext<br>
                            ${result.errorMessage ?: ""}
                        """.trimIndent()
                    )
                    .notify(project)
            } else {
                Notifications
                    .info(
                        "Fetched remote Spring beans",
                        """
                            Web context: $webContext<br>
                            Beans: ${beans.size}
                        """.trimIndent()
                    )
                    .notify(project)
            }
        }
    }

    fun fetchWebApplicationContexts(virtualFile: VirtualFile) {
        val server = HacExecConnectionService.getInstance(project).activeConnection
        val groovyScript = readResource("scripts/groovy-loadWebContexts.groovy")
        val context = GroovyExecContext(
            connection = server,
            executionTitle = "Fetching web contexts...",
            content = groovyScript,
            transactionMode = TransactionMode.ROLLBACK,
            timeout = server.timeout,
        )

        GroovyExecClient.getInstance(project).execute(
            context,
            beforeCallback = {
                setFetchingWebContexts(virtualFile, true)
                virtualFile.groovyExecContextSettings = virtualFile.groovyExecContextSettings
                    ?.copy(webContext = null)
            },
            onError = { _, ex ->
                setFetchingWebContexts(virtualFile, false)
                Notifications
                    .error("Unable to load web contexts", ex.message ?: "")
                    .notify(project)
            }
        ) { _, result ->
            val contexts = result.result
                ?.split("|")
                ?.filter { it.isNotBlank() }
                ?.sorted()

            setFetchingWebContexts(virtualFile, false)
            setWebContexts(virtualFile, contexts)

            Notifications
                .info("Found ${contexts?.size ?: 0} web contexts")
                .notify(project)
        }
    }

    fun getSettings(virtualFile: VirtualFile, fallback: (() -> Settings)? = null): Settings = virtualFile.groovyExecContextSettings
        ?: fallback?.invoke()
        ?: run {
            val activeConnection = HacExecConnectionService.getInstance(project).activeConnection
            GroovyExecContext.defaultSettings(activeConnection)
        }

    fun setSettings(virtualFile: VirtualFile, settings: Settings) {
        virtualFile.groovyExecContextSettings = settings
    }

    fun getTransactionMode(virtualFile: VirtualFile, project: Project) = virtualFile.groovyExecContextSettings
        ?.transactionMode
        ?: project.yDeveloperSettings.groovySettings.transactionMode

    fun setTransactionMode(virtualFile: VirtualFile, mode: TransactionMode) {
        virtualFile.groovyExecContextSettings = getSettings(virtualFile).copy(transactionMode = mode)
    }

    fun getReplicaAwareContext(virtualFile: VirtualFile) = virtualFile.groovyExecContextSettings?.replicaContext
        ?: GroovyReplicaAwareContext.auto()

    fun setReplicaAwareContext(virtualFile: VirtualFile, context: GroovyReplicaAwareContext) {
        virtualFile.groovyExecContextSettings = getSettings(virtualFile).copy(replicaContext = context)
    }

    fun getWebContexts(virtualFile: VirtualFile) = virtualFile.getUserData(KEY_WEB_CONTEXTS)
    fun setWebContexts(virtualFile: VirtualFile, webContexts: Collection<String>?) = virtualFile.putUserData(KEY_WEB_CONTEXTS, webContexts)

    fun isFetchingWebContexts(virtualFile: VirtualFile) = virtualFile.getUserData(KEY_WEB_CONTEXTS_FETCHING) ?: false
    private fun setFetchingWebContexts(virtualFile: VirtualFile, state: Boolean) = virtualFile.putUserData(KEY_WEB_CONTEXTS_FETCHING, state)

    fun getActiveWebContext(virtualFile: VirtualFile) = virtualFile.groovyExecContextSettings?.webContext
        ?: GroovyExecConstants.DEFAULT_WEB_CONTEXT

    fun activateWebContext(project: Project, virtualFile: VirtualFile, webContext: String) {
        val activeWebContext = if (webContext != GroovyExecConstants.DEFAULT_WEB_CONTEXT) webContext
        else null

        virtualFile.groovyExecContextSettings = getSettings(virtualFile).copy(webContext = activeWebContext)

        val currentSpringContextMode = virtualFile.getSpringContextMode(project)
        if (currentSpringContextMode == SpringContextMode.REMOTE) {
            fetchRemoteSpringBeans(virtualFile)
        }
    }

    private var VirtualFile.groovyExecContextSettings
        get() = this.getUserData(KEY_EXECUTION_SETTINGS)
        set(value) {
            this.putUserData(KEY_EXECUTION_SETTINGS, value)
        }

    companion object {
        private val KEY_EXECUTION_SETTINGS = Key.create<Settings>("sap.cx.groovy.execution.settings")
        private val KEY_WEB_CONTEXTS = Key.create<Collection<String>>("sap.cx.groovy.execution.webContexts")
        private val KEY_WEB_CONTEXTS_FETCHING = Key.create<Boolean>("sap.cx.groovy.execution.webContexts.fetching")

        fun getInstance(project: Project): GroovyExecService = project.service()
    }
}