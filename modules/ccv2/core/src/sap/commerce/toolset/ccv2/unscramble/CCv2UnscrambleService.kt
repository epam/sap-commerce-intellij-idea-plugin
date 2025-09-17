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

package sap.commerce.toolset.ccv2.unscramble

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*

@Service
class CCv2UnscrambleService {

    fun canHandle(text: String) = getThrown(text) != null

    fun buildStackTraceString(text: String): String? = getThrown(text)
        ?.let { buildStackTraceString(it) }

    private fun buildStackTraceString(obj: JsonObject, indent: String = ""): String = buildString {
        val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: "java.lang.Exception"
        val message = obj["message"]?.jsonPrimitive?.contentOrNull ?: ""

        append("$indent$name: $message\n")

        obj["extendedStackTrace"]
            ?.jsonArray
            ?.map { it.jsonObject }
            ?.forEach {
                val className = it["class"]?.jsonPrimitive?.contentOrNull ?: "UnknownClass"
                val methodName = it["method"]?.jsonPrimitive?.contentOrNull ?: "unknownMethod"
                val fileName = it["file"]?.jsonPrimitive?.contentOrNull ?: "UnknownFile"
                val lineNumber = it["line"]?.jsonPrimitive?.intOrNull ?: -1
                val lineStr = if (lineNumber >= 0) "$fileName:$lineNumber" else fileName

                append("$indent\tat $className.$methodName($lineStr)\n")
            }


        obj["cause"]
            ?.takeUnless { it is JsonNull }
            ?.jsonObject
            ?.let {
                append("${indent}Caused by: ${buildStackTraceString(it, indent)}")
            }
    }


    private fun getThrown(text: String) = try {
        Json.parseToJsonElement(text)
            .jsonObject["thrown"]
            ?.takeUnless { it is JsonNull }
            ?.jsonObject
            ?.takeIf { it.containsKey("extendedStackTrace") }
    } catch (_: SerializationException) {
        null
    }

    companion object {
        fun getInstance(): CCv2UnscrambleService = application.service()
    }
}