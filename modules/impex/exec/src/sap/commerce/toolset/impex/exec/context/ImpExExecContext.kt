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

package sap.commerce.toolset.impex.exec.context

import com.intellij.openapi.util.Key
import org.apache.commons.lang3.BooleanUtils
import sap.commerce.toolset.exec.context.ExecContext
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import java.nio.charset.StandardCharsets

data class ImpExExecContext(
    val connection: HacConnectionSettingsState,
    private val content: String = "",
    val dialect: ImpExDialect = ImpExDialect.IMPEX,
    val executionMode: ImpExExecutionMode = ImpExExecutionMode.IMPORT,
    val validationMode: ImpExValidationMode,
    val maxThreads: Int,
    val timeout: Int,
    val encoding: String,
    val legacyMode: ImpExToggle,
    val enableCodeExecution: ImpExToggle,
    val sldEnabled: ImpExToggle,
    val distributedMode: ImpExToggle,
) : ExecContext {

    constructor(
        connection: HacConnectionSettingsState,
        content: String = "",
        dialect: ImpExDialect = ImpExDialect.IMPEX,
        executionMode: ImpExExecutionMode = ImpExExecutionMode.IMPORT,
        settings: Settings,
    ) : this(
        connection = connection,
        content = content,
        dialect = dialect,
        validationMode = settings.validationMode,
        maxThreads = settings.maxThreads,
        timeout = settings.timeout,
        encoding = settings.encoding,
        legacyMode = settings.legacyMode,
        enableCodeExecution = settings.enableCodeExecution,
        sldEnabled = settings.sldEnabled,
        distributedMode = settings.distributedMode,
    )

    override val executionTitle: String
        get() = when (executionMode) {
            ImpExExecutionMode.IMPORT -> "Importing ${dialect.title} on the remote SAP Commerce instance…"
            ImpExExecutionMode.VALIDATE -> "Validating ${dialect.title} on the remote SAP Commerce instance…"
        }

    fun params(): Map<String, String> = buildMap {
        put("scriptContent", content)
        put("validationEnum", validationMode.name)
        put("encoding", encoding)
        put("maxThreads", maxThreads.toString())
        put("legacyMode", BooleanUtils.toStringTrueFalse(legacyMode.booleanValue))
        put("enableCodeExecution", BooleanUtils.toStringTrueFalse(enableCodeExecution.booleanValue))
        put("sldEnabled", BooleanUtils.toStringTrueFalse(sldEnabled.booleanValue))
        put("_sldEnabled", sldEnabled.value)
        put("_enableCodeExecution", enableCodeExecution.value)
        put("_legacyMode", legacyMode.value)
        put("_distributedMode", distributedMode.value)
    }

    data class Settings(
        val validationMode: ImpExValidationMode,
        val maxThreads: Int,
        override val timeout: Int,
        val encoding: String,
        val legacyMode: ImpExToggle,
        val enableCodeExecution: ImpExToggle,
        val sldEnabled: ImpExToggle,
        val distributedMode: ImpExToggle,
    ) : ExecContext.Settings {
        override fun mutable() = Mutable(
            validationMode = validationMode,
            maxThreads = maxThreads,
            timeout = timeout,
            encoding = encoding,
            legacyMode = legacyMode,
            enableCodeExecution = enableCodeExecution,
            sldEnabled = sldEnabled,
            distributedMode = distributedMode,
        )

        data class Mutable(
            var validationMode: ImpExValidationMode,
            var maxThreads: Int,
            override var timeout: Int,
            var encoding: String,
            var legacyMode: ImpExToggle,
            var enableCodeExecution: ImpExToggle,
            var sldEnabled: ImpExToggle,
            var distributedMode: ImpExToggle,
        ) : ExecContext.Settings.Mutable {
            override fun immutable() = Settings(
                validationMode = validationMode,
                maxThreads = maxThreads,
                timeout = timeout,
                encoding = encoding,
                legacyMode = legacyMode,
                enableCodeExecution = enableCodeExecution,
                sldEnabled = sldEnabled,
                distributedMode = distributedMode,
            )
        }
    }

    companion object {
        val KEY_EXECUTION_SETTINGS = Key.create<Settings>("sap.cx.impex.execution.settings")

        fun defaultSettings(connectionSettings: HacConnectionSettingsState): Settings =
            Settings(
                validationMode = ImpExValidationMode.IMPORT_STRICT,
                maxThreads = 20,
                timeout = connectionSettings.timeout,
                encoding = StandardCharsets.UTF_8.name(),
                legacyMode = ImpExToggle.OFF,
                enableCodeExecution = ImpExToggle.ON,
                sldEnabled = ImpExToggle.OFF,
                distributedMode = ImpExToggle.OFF,
            )
    }
}