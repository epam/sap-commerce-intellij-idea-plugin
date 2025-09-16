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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class CCv2JsonUnscrambleHelper {

    private val mapper = ObjectMapper()

    fun canHandle(text: String = ""): Boolean {
        try {
            val rootNode: JsonNode = mapper.readTree(text);
            val thrownNode = rootNode["thrown"]

            return thrownNode != null && thrownNode.has("extendedStackTrace") && thrownNode.has("cause")
        } catch (e: Exception) {
            return false;
        }
    }

    fun buildStackTraceString(text: String = ""): String? {
        if (canHandle(text)) {
            val rootNode: JsonNode = mapper.readTree(text)
            val thrownNode = rootNode["thrown"]

            return buildStackTraceString(thrownNode)
        }
        return null;
    }

    private fun buildStackTraceString(node: JsonNode?, indent: String = ""): String {
        if (node == null) return ""

        val name = node["name"]?.asText() ?: "java.lang.Exception"
        val message = node["message"]?.asText() ?: ""

        val sb = StringBuilder()
        sb.append("$indent$name: $message\n")

        // Stack trace elements
        val stackTraceNodes = node["extendedStackTrace"]
        if (stackTraceNodes != null) {
            for (elem in stackTraceNodes) {
                val className = elem["class"]?.asText() ?: "UnknownClass"
                val methodName = elem["method"]?.asText() ?: "unknownMethod"
                val fileName = elem["file"]?.asText() ?: "UnknownFile"
                val lineNumber = elem["line"]?.asInt(-1) ?: -1
                val lineStr = if (lineNumber >= 0) "$fileName:$lineNumber" else fileName
                sb.append("$indent\tat $className.$methodName($lineStr)\n")
            }
        }

        // Cause (recursive)
        val causeNode = node["cause"]
        if (causeNode != null && !causeNode.isNull) {
            // Correct usage: just use the existing 'indent' variable
            sb.append("${indent}Caused by: ${buildStackTraceString(causeNode, indent)}")
        }

        return sb.toString()
    }
}