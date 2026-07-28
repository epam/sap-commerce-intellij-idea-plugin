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

package sap.commerce.toolset.ccv2.mcp.dto

import kotlinx.serialization.Serializable

@Serializable
data class CCv2MOperationResultDto(
    val subscription: String,
    val success: Boolean,
    val code: String? = null,
    val message: String,
)

@Serializable
data class CCv2MUnscrambleResultDto(
    val success: Boolean,
    val stackTrace: String? = null,
    val message: String,
)

@Serializable
data class CCv2MBuildLogsDto(
    val subscription: String,
    val buildCode: String,
    val files: List<CCv2MLogFileDto>,
)

@Serializable
data class CCv2MLogFileDto(
    val name: String,
    val content: String,
)
