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

package sap.commerce.toolset.groovy.exec.context

import org.apache.commons.lang3.BooleanUtils
import sap.commerce.toolset.exec.context.ExecContext
import sap.commerce.toolset.exec.context.ReplicaContext
import sap.commerce.toolset.groovy.settings.GroovyDeveloperSettings
import sap.commerce.toolset.groovy.settings.state.GroovyExecExceptionHandling
import sap.commerce.toolset.groovy.settings.state.GroovyExecMode
import sap.commerce.toolset.hac.HacExecConstants
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.settings.state.TransactionMode

data class GroovyExecContext(
    val connection: HacConnectionSettingsState,
    override val executionTitle: String = DEFAULT_TITLE,
    val content: String,
    val timeout: Int,
    val exceptionHandling: GroovyExecExceptionHandling = GroovyExecExceptionHandling.FULL_STACKTRACE,
    val transactionMode: TransactionMode,
    val webContext: String? = null,
    val replicaContext: ReplicaContext? = null,
    val execMode: GroovyExecMode = GroovyExecMode.DIRECT
) : ExecContext {

    constructor(
        connection: HacConnectionSettingsState,
        executionTitle: String = DEFAULT_TITLE,
        content: String,
        replicaContext: ReplicaContext? = null,
        settings: Settings,
    ) : this(
        connection = connection,
        executionTitle = executionTitle,
        content = content,
        timeout = settings.timeout,
        exceptionHandling = settings.exceptionHandling,
        execMode = settings.execMode,
        transactionMode = settings.transactionMode,
        webContext = settings.webContext,
        replicaContext = replicaContext,
    )

    fun params(): Map<String, String> = buildMap {
        put("scriptType", "groovy")
        put("commit", BooleanUtils.toStringTrueFalse(transactionMode == TransactionMode.COMMIT))
        put("script", content)
    }

    data class Settings(
        override val timeout: Int,
        val webContext: String? = null,
        val exceptionHandling: GroovyExecExceptionHandling,
        val execMode: GroovyExecMode,
        val transactionMode: TransactionMode,
        val replicaContext: GroovyReplicaAwareContext = GroovyReplicaAwareContext.auto()
    ) : ExecContext.Settings {
        override fun mutable() = Mutable(
            timeout = timeout,
            transactionMode = transactionMode,
            replicaContext = replicaContext,
            webContext = webContext,
            exceptionHandling = exceptionHandling,
            execMode = this@Settings.execMode,
        )

        data class Mutable(
            override var timeout: Int,
            var exceptionHandling: GroovyExecExceptionHandling,
            var execMode: GroovyExecMode,
            var transactionMode: TransactionMode,
            var replicaContext: GroovyReplicaAwareContext,
            var webContext: String?
        ) : ExecContext.Settings.Mutable {
            override fun immutable() = Settings(
                timeout = timeout,
                transactionMode = transactionMode,
                replicaContext = replicaContext,
                webContext = webContext,
                exceptionHandling = exceptionHandling,
                execMode = execMode,
            )
        }
    }

    companion object {
        const val DEFAULT_TITLE = "Executing Groovy script on the remote SAP Commerce instance..."

        fun defaultSettings(connectionSettings: HacConnectionSettingsState? = null, groovySettings: GroovyDeveloperSettings) = Settings(
            timeout = connectionSettings?.timeout ?: HacExecConstants.DEFAULT_TIMEOUT,
            transactionMode = groovySettings.transactionMode,
            execMode = groovySettings.execMode,
            exceptionHandling = groovySettings.exceptionHandling,
        )
    }
}