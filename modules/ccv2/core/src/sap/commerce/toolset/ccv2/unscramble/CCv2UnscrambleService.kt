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

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import kotlinx.serialization.json.*

@Service
class CCv2UnscrambleService {

    fun canHandle(text: String = ""): Boolean {
        try {
            val rootNode: JsonObject = Json.parseToJsonElement(text).jsonObject
            val thrownNode = rootNode["thrown"]?.jsonObject

            return thrownNode != null && thrownNode.containsKey("extendedStackTrace") && thrownNode.containsKey("cause")
        } catch (e: Exception) {
            return false;
        }
    }

    fun buildStackTraceString(text: String = ""): String? {
        if (canHandle(text)) {
            val rootNode: JsonObject = Json.parseToJsonElement(text).jsonObject
            val thrownNode = rootNode["thrown"]

            return buildStackTraceString(thrownNode)
        }
        return null;
    }

    private fun buildStackTraceString(node: JsonElement?, indent: String = ""): String {
        if (node == null || node is JsonNull) return ""

        val obj = node.jsonObject  // <-- ensure it's a JsonObject

        val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: "java.lang.Exception"
        val message = obj["message"]?.jsonPrimitive?.contentOrNull ?: ""

        val sb = StringBuilder()
        sb.append("$indent$name: $message\n")

        val stackTraceNodes = obj["extendedStackTrace"]?.jsonArray
        if (stackTraceNodes != null) {
            for (elem in stackTraceNodes) {
                val elemObj = elem.jsonObject
                val className = elemObj["class"]?.jsonPrimitive?.contentOrNull ?: "UnknownClass"
                val methodName = elemObj["method"]?.jsonPrimitive?.contentOrNull ?: "unknownMethod"
                val fileName = elemObj["file"]?.jsonPrimitive?.contentOrNull ?: "UnknownFile"
                val lineNumber = elemObj["line"]?.jsonPrimitive?.intOrNull ?: -1
                val lineStr = if (lineNumber >= 0) "$fileName:$lineNumber" else fileName
                sb.append("$indent\tat $className.$methodName($lineStr)\n")
            }
        }

        val causeNode = obj["cause"]
        if (causeNode != null && causeNode !is JsonNull) {
            sb.append("${indent}Caused by: ${buildStackTraceString(causeNode, indent)}")
        }

        return sb.toString()
    }

    companion object {
        fun getInstance(): CCv2UnscrambleService = application.service()
    }
}