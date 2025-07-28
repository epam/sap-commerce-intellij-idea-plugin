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

package com.intellij.idea.plugin.hybris.tools.remote.execution.impex

import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import org.apache.commons.lang3.BooleanUtils
import java.nio.charset.StandardCharsets

data class ImpExExecutionContext(
    private val content: String = "",
    val dialect: Dialect = Dialect.IMPEX,
    val executionMode: ExecutionMode = ExecutionMode.IMPORT,
    val settings: Settings
) : ExecutionContext {

    override val executionTitle: String
        get() = when (executionMode) {
            ExecutionMode.IMPORT -> "Importing ${dialect.title} on the remote SAP Commerce instance…"
            ExecutionMode.VALIDATE -> "Validating ${dialect.title} on the remote SAP Commerce instance…"
        }

    fun params(): Map<String, String> = buildMap {
        put("scriptContent", content)
        put("validationEnum", settings.validationMode.name)
        put("encoding", settings.encoding)
        put("maxThreads", settings.maxThreads.toString())
        put("legacyMode", BooleanUtils.toStringTrueFalse(settings.legacyMode.booleanValue))
        put("enableCodeExecution", BooleanUtils.toStringTrueFalse(settings.enableCodeExecution.booleanValue))
        put("sldEnabled", BooleanUtils.toStringTrueFalse(settings.sldEnabled.booleanValue))
        put("_sldEnabled", settings.sldEnabled.value)
        put("_enableCodeExecution", settings.enableCodeExecution.value)
        put("_legacyMode", settings.legacyMode.value)
        put("_distributedMode", settings.distributedMode.value)
    }

    data class Settings(
        val validationMode: ValidationMode,
        val maxThreads: Int,
        val timeout: Int,
        val encoding: String,
        val legacyMode: Toggle,
        val enableCodeExecution: Toggle,
        val sldEnabled: Toggle,
        val distributedMode: Toggle,
    ) : ExecutionContext.Settings {
        override fun modifiable() = ModifiableSettings(
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

    data class ModifiableSettings(
        var validationMode: ValidationMode,
        var maxThreads: Int,
        var timeout: Int,
        var encoding: String,
        var legacyMode: Toggle,
        var enableCodeExecution: Toggle,
        var sldEnabled: Toggle,
        var distributedMode: Toggle,
    ) : ExecutionContext.ModifiableSettings {
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

    enum class Dialect(val title: String) {
        IMPEX("ImpEx"),
        ACL("ACL")
    }

    enum class ExecutionMode {
        IMPORT, VALIDATE
    }

    enum class ValidationMode(val title: String) {
        IMPORT_STRICT("Strict"),
        IMPORT_RELAXED("Relaxed"),
    }

    enum class Toggle(val value: String, val booleanValue: Boolean) {
        ON("on", true), OFF("off", false);

        companion object {
            fun of(value: Boolean) = if (value) ON else OFF
        }
    }

    companion object {
        val DEFAULT_SETTINGS by lazy {
            Settings(
                validationMode = ValidationMode.IMPORT_STRICT,
                maxThreads = 20,
                timeout = HybrisHacHttpClient.DEFAULT_HAC_TIMEOUT,
                encoding = StandardCharsets.UTF_8.name(),
                legacyMode = Toggle.OFF,
                enableCodeExecution = Toggle.ON,
                sldEnabled = Toggle.OFF,
                distributedMode = Toggle.OFF,
            )
        }
    }
}
