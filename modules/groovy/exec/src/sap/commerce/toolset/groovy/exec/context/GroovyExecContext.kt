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

package sap.commerce.toolset.groovy.exec.context

import com.intellij.openapi.util.Key
import org.apache.commons.lang3.BooleanUtils
import sap.commerce.toolset.exec.context.ExecContext
import sap.commerce.toolset.exec.context.ReplicaContext
import sap.commerce.toolset.hac.HacExecConstants
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.settings.state.TransactionMode

data class GroovyExecContext(
    val connection: HacConnectionSettingsState,
    override val executionTitle: String = DEFAULT_TITLE,
    private val content: String,
    val settings: Settings,
    val replicaContext: ReplicaContext? = null
) : ExecContext {

    fun params(): Map<String, String> = buildMap {
        put("scriptType", "groovy")
        put("commit", BooleanUtils.toStringTrueFalse(settings.transactionMode == TransactionMode.COMMIT))
        put("script", content)
    }

    data class Settings(
        override val timeout: Int,
        val transactionMode: TransactionMode = TransactionMode.ROLLBACK,
        val replicaContext: GroovyReplicaAwareContext = GroovyReplicaAwareContext.auto()
    ) : ExecContext.Settings {
        override fun mutable() = Mutable(
            timeout = timeout,
            transactionMode = transactionMode,
            replicaContext = replicaContext,
        )

        data class Mutable(
            override var timeout: Int,
            var transactionMode: TransactionMode,
            var replicaContext: GroovyReplicaAwareContext
        ) : ExecContext.Settings.Mutable {
            override fun immutable() = Settings(
                timeout = timeout,
                transactionMode = transactionMode,
                replicaContext = replicaContext,
            )
        }
    }

    companion object {
        val KEY_EXECUTION_SETTINGS = Key.create<Settings>("sap.cx.groovy.execution.settings")
        const val DEFAULT_TITLE = "Executing Groovy script on the remote SAP Commerce instance..."

        fun defaultSettings(connectionSettings: HacConnectionSettingsState? = null) = Settings(
            timeout = connectionSettings?.timeout ?: HacExecConstants.DEFAULT_TIMEOUT,
            transactionMode = TransactionMode.ROLLBACK
        )
    }
}