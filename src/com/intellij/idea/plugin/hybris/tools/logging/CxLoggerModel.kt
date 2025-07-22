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

package com.intellij.idea.plugin.hybris.tools.logging

data class CxLoggerModel(
    val name: String,
    val effectiveLevel: String,
    val parentName: String? = null,
    val inherited: Boolean = false,
    private val rootParent: String? = null
) {
    val root: String
        get() = rootParent ?: parentName ?: "root"

    override fun toString(): String {
        return "CxLoggerModel(name='$name', effectiveLevel='$effectiveLevel', parentName=$parentName, inherited=$inherited, root=${root})"
    }
}

data class CxLoggersStorage(private val loggers: MutableMap<String, CxLoggerModel>) {

    fun get(loggerIdentifier: String): CxLoggerModel {
        return loggers[loggerIdentifier] ?: createLoggerModel(loggerIdentifier)
    }

    private fun _get(loggerIdentifier: String): CxLoggerModel {
        return loggers[loggerIdentifier] ?: createLoggerModel(loggerIdentifier)
    }

    private fun createLoggerModel(loggerIdentifier: String): CxLoggerModel {
        val parentLogger = loggerIdentifier.substringBeforeLast('.', "")
            .takeIf(String::isNotEmpty)
            ?.let { _get(it) }
            ?: loggers["root"]
            ?: CxLoggerModel(name = "root", effectiveLevel = "undefined")

        return loggers.getOrPut(loggerIdentifier) {
            CxLoggerModel(
                loggerIdentifier,
                parentLogger.effectiveLevel,
                if (parentLogger.inherited) parentLogger.root else parentLogger.name,
                true,
                parentLogger.root
            )
        }
    }
}