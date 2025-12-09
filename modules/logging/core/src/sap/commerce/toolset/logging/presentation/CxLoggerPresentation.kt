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

package sap.commerce.toolset.logging.presentation

import sap.commerce.toolset.logging.CxLogConstants
import sap.commerce.toolset.logging.CxLogLevel

data class CxLoggerPresentation(
    private val effectiveLevel: String,
    val name: String,
    val parentName: String?,
    val inherited: Boolean,
    val level: CxLogLevel = CxLogLevel.of(effectiveLevel),
) {
    val presentableParent
        get() = this.parentName
            ?.takeIf { it.isNotEmpty() }
            ?.takeIf { it != CxLogConstants.ROOT_LOGGER_NAME }
            ?.let { "child of $it" }

    companion object {
        fun of(
            name: String,
            effectiveLevel: String,
            parentName: String? = null,
            inherited: Boolean = false
        ): CxLoggerPresentation = CxLoggerPresentation(
            name = name,
            effectiveLevel = effectiveLevel,
            parentName = if (name == CxLogConstants.ROOT_LOGGER_NAME) null else parentName,
            inherited = inherited,
        )

        fun inherited(name: String, parentLogger: CxLoggerPresentation): CxLoggerPresentation = of(
            name = name,
            effectiveLevel = parentLogger.effectiveLevel,
            parentName = parentLogger.name,
            inherited = true,
        )

        fun rootFallback() = of(name = CxLogConstants.ROOT_LOGGER_NAME, effectiveLevel = "undefined")
    }
}
