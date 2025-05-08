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

package com.intellij.idea.plugin.hybris.tools.logging

import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FETCH_LOGGERS_STATE_GROOVY_SCRIPT = """
    import de.hybris.platform.core.Registry
    import de.hybris.platform.hac.facade.HacLog4JFacade
    import java.util.stream.Collectors
    
    Registry.applicationContext.getBean("hacLog4JFacade", HacLog4JFacade.class).getLoggers().stream()
            .map { it -> it.name + " | " + it.parentName + " | " + it.effectiveLevel }
            .collect(Collectors.joining("\n"))
"""

@Service(Service.Level.PROJECT)
class CxLoggerAccess(private val project: Project, private val coroutineScope: CoroutineScope) : UserDataHolderBase() {
    companion object {
        fun getInstance(project: Project): CxLoggerAccess = project.getService(CxLoggerAccess::class.java)
    }

    private var fetching: Boolean = false
    val canRefresh: Boolean
        get() = !fetching

    val loggers: Map<String, CxLoggerModel> = mutableMapOf()

    fun refresh() {
        fetching = true
        coroutineScope.launch {
            val server = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
            withBackgroundProgress(project, "Fetching Loggers from SAP Commerce [${server.shortenConnectionName()}]...", true) {
                delay(10000)
                val result = reportProgress { progressReporter ->
                    HybrisHacHttpClient.getInstance(project).executeGroovyScript(
                        project,
                        FETCH_LOGGERS_STATE_GROOVY_SCRIPT.trimIndent(),
                        null,
                        false,
                        AbstractHybrisHacHttpClient.DEFAULT_HAC_TIMEOUT
                    )
                }

                try {
                    if (result.statusCode == 200) {
                        val newLoggers = result.result
                            .split("\n")
                            .map { it -> it.split(" | ") }
                            .filter { it.size == 3 }
                            .map { CxLoggerModel(it[0], it[2], it[1]) }
                            .associateBy { it.name }

                        loggers.asSafely<MutableMap<String, CxLoggerModel>>()
                            ?.let {
                                it.clear()
                                it.putAll(newLoggers)
                            }

                        notifySuccess()
                    } else {
                        loggers.asSafely<MutableMap<String, CxLoggerModel>>()?.clear()
                        notifyError()
                    }

                } catch (e: Exception) {
                    loggers.asSafely<MutableMap<String, CxLoggerModel>>()?.clear()
                    notifyError()
                }

                edtWriteAction {
                    PsiDocumentManager.getInstance(project).reparseFiles(emptyList(), true)
                }

                fetching = false
            }
        }
    }

    private fun notifyError() {
        val server = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        Notifications
            .create(
                NotificationType.ERROR,
                "Loading loggers from SAP Commerce...",
                """
                            <p>Failed to fetch logger states.</p>
                            <p>${server.shortenConnectionName()}</p>
                        """.trimIndent()
            )
            .hideAfter(5)
            .notify(project)
    }

    private fun notifySuccess() {
        val server = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        Notifications
            .create(
                NotificationType.INFORMATION,
                "Loading loggers from SAP Commerce...",
                """
                            <p>Loggers state is fetched.</p>
                            <p>${server.shortenConnectionName()}</p>
                        """.trimIndent()
            )
            .hideAfter(5)
            .notify(project)
    }
}