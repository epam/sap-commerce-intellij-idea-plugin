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
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

data class ImpExExecutionContext(
    private val content: String = "",
    private val validationMode: ValidationMode = ValidationMode.IMPORT_STRICT,
    private val encoding: Charset = StandardCharsets.UTF_8,
    private val maxThreads: Int = 20,
    private val legacyMode: Toggle = Toggle.OFF,
    private val enableCodeExecution: Toggle = Toggle.ON,
    private val sldEnabled: Toggle = Toggle.ON,
    private val distributedMode: Toggle = Toggle.ON,
    val executionMode: ExecutionMode = ExecutionMode.IMPORT,
    val timeout: Int = HybrisHacHttpClient.DEFAULT_HAC_TIMEOUT
) : ExecutionContext {
    fun params(): Map<String, String> = buildMap {
        put("scriptContent", content)
        put("validationEnum", validationMode.name)
        put("encoding", encoding.name())
        put("maxThreads", maxThreads.toString())
        put("legacyMode", BooleanUtils.toStringTrueFalse(legacyMode == Toggle.ON))
        put("enableCodeExecution", BooleanUtils.toStringTrueFalse(enableCodeExecution == Toggle.ON))
        put("sldEnabled", BooleanUtils.toStringTrueFalse(sldEnabled == Toggle.ON))
        put("_sldEnabled", sldEnabled.value)
        put("_enableCodeExecution", enableCodeExecution.value)
        put("_legacyMode", legacyMode.value)
        put("_distributedMode", distributedMode.value)
    }
}

enum class ExecutionMode {
    IMPORT, VALIDATE
}

enum class ValidationMode {
    IMPORT_STRICT, IMPORT_RELAXED
}

enum class Toggle(val value: String) {
    ON("on"), OFF("off")
}
