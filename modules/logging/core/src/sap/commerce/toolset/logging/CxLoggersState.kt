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

package sap.commerce.toolset.logging

class CxLoggersState {

    private val _loggers: MutableMap<String, CxLoggerModel>
    private var _initialized: Boolean = false

    val initialized: Boolean
        get() = _initialized

    constructor(loggers: Map<String, CxLoggerModel>) {
        this._loggers = loggers.toMutableMap()
        _initialized = !loggers.isEmpty()
    }

    constructor() : this(mapOf())

    fun get(loggerIdentifier: String): CxLoggerModel = _loggers[loggerIdentifier]
        ?: createLoggerModel(loggerIdentifier)

    fun update(loggers: Map<String, CxLoggerModel>) {
        synchronized(loggers) {
            this._loggers.clear()
            this._loggers.putAll(loggers)
            _initialized = true
        }
    }

    fun get() : Map<String, CxLoggerModel>? = if (_initialized) _loggers else null

    fun clear() {
        synchronized(_loggers) {
            this._loggers.clear()
            _initialized = false
        }
    }

    private fun createLoggerModel(loggerIdentifier: String): CxLoggerModel {
        val parentLogger = loggerIdentifier.substringBeforeLast('.', "")
            .takeIf { it.isNotBlank() }
            ?.let { get(it) }
            ?: _loggers[CxLoggersConstants.ROOT_LOGGER_NAME]
            ?: CxLoggerModel.rootFallback()

        return _loggers.getOrPut(loggerIdentifier) {
            CxLoggerModel.inherited(loggerIdentifier, parentLogger)
        }
    }
}