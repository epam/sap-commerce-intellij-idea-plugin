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

package com.intellij.idea.plugin.hybris.tools.remote.http.impex

import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import org.apache.commons.lang3.BooleanUtils
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

data class ImpExExecutionContext(
    private val content: String = "",
    private val validationMode: ValidationMode = ValidationMode.IMPORT_STRICT,
    private val encoding: Charset = StandardCharsets.UTF_8,
    private val maxThreads: Int = 20,
    private val legacyMode: Boolean = false,
    private val enableCodeExecution: Boolean = true,
    private val sldEnabled: Boolean = true,
    private val _legacyMode: Toggle = Toggle.ON,
    private val _enableCodeExecution: Toggle = Toggle.ON,
    private val _distributedMode: Toggle = Toggle.ON,
    private val _sldEnabled: Toggle = Toggle.ON,
    val timeout: Int = AbstractHybrisHacHttpClient.DEFAULT_HAC_TIMEOUT
) {
    fun params(): Map<String, String> = buildMap {
        put("scriptContent", content)
        put("validationEnum", validationMode.name)
        put("encoding", encoding.name())
        put("maxThreads", maxThreads.toString())
        put("_legacyMode", _legacyMode.value)
        put("_enableCodeExecution", _enableCodeExecution.value)
        put("_distributedMode", _distributedMode.value)
        put("_sldEnabled", _sldEnabled.value)
        put("legacyMode", BooleanUtils.toStringTrueFalse(legacyMode))
        put("enableCodeExecution", BooleanUtils.toStringTrueFalse(enableCodeExecution))
        put("sldEnabled", BooleanUtils.toStringTrueFalse(sldEnabled))
    }
}

enum class ValidationMode {
    IMPORT_STRICT, IMPORT_RELAXED
}

enum class Toggle(val value: String) {
    ON("on"), OFF("off")
}
