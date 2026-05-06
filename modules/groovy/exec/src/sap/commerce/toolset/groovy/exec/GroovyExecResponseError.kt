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

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class GroovyExecResponseError(
    val message: String,
    val details: String? = null,
) {
    companion object {
        fun from(json: JsonElement): GroovyExecResponseError? {
            val errorText = json.jsonObject[GroovyExecConstants.RESPONSE_STACKTRACE_TEXT]
                ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                ?: return null

            if (errorText.contains("Script compilation has failed [")) return GroovyExecResponseError(
                message = "Script compilation has failed",
                details = errorText
                    .replace("Script compilation has failed [", "")
                    .let { it.substring(0, it.length - 1) }
            )

            return errorText.indexOf("\tat")
                .takeIf { it > -1 }
                ?.let {
                    GroovyExecResponseError(
                        message = errorText.substring(0, it),
                        details = errorText.replace("\t", "    ")
                    )
                }
                ?: GroovyExecResponseError(message = errorText)
        }
    }
}