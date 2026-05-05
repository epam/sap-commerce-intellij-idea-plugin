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

package sap.commerce.toolset.groovy

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.lang.resolve.RemoteSpringBean
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.readResource
import sap.commerce.toolset.settings.state.SpringContextMode
import sap.commerce.toolset.settings.state.TransactionMode

@Service(Service.Level.PROJECT)
class GroovyExecService(private val project: Project) {

    private var fetching = false

    fun fetchRemoteSpringBeans(virtualFile: VirtualFile) {
        val webContext = virtualFile.groovyWebContext

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
                        """.trimIndent())
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
                virtualFile.groovyWebContextsFetching = true
                virtualFile.groovyExecContextSettings = virtualFile.groovyExecContextSettings
                    ?.copy(webContext = null)
            },
            onError = { _, ex ->
                virtualFile.groovyWebContextsFetching = false
                Notifications
                    .error("Unable to load web contexts", ex.message ?: "")
                    .notify(project)
            }
        ) { _, result ->
            val contexts = result.result
                ?.split("|")
                ?.filter { it.isNotBlank() }
                ?.sorted()

            virtualFile.groovyWebContextsFetching = false
            virtualFile.groovyWebContexts = contexts

            Notifications
                .info("Found ${contexts?.size ?: 0} web contexts")
                .notify(project)
        }
    }

    fun activateWebContext(virtualFile: VirtualFile, webContext: String) {
        val activeWebContext = if (webContext != GroovyExecConstants.DEFAULT_WEB_CONTEXT) webContext
        else null

        virtualFile.groovyExecContextSettings = virtualFile.groovyExecContextSettings {
            val activeConnection = HacExecConnectionService.getInstance(project).activeConnection
            GroovyExecContext.defaultSettings(activeConnection)
        }.copy(webContext = activeWebContext)

        val currentSpringContextMode = virtualFile.getCurrentSpringContextMode(project)
        if (currentSpringContextMode == SpringContextMode.REMOTE) {
            fetchRemoteSpringBeans(virtualFile)
        }
    }

    companion object {
        fun getInstance(project: Project): GroovyExecService = project.service()
    }
}