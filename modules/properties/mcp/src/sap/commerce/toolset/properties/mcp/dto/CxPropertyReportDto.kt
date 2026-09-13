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

package sap.commerce.toolset.properties.mcp.dto

import kotlinx.serialization.Serializable

/**
 * Where one property comes from, which declaration the platform applies and what the instance makes of it.
 *
 * @param effectiveValue the value the platform applies, with `${...}` placeholders expanded.
 * @param rawValue the winning declaration as written, which differs from [effectiveValue] only when it has placeholders.
 * @param remoteError why the instance could not be asked, when it could not; the local half of the report still stands.
 */
@Serializable
data class CxPropertyReportDto(
    val key: String,
    val declared: Boolean,
    val effectiveValue: String? = null,
    val rawValue: String? = null,
    val declarations: List<CxPropertyDeclarationDto> = emptyList(),
    val rule: CxPropertyRuleDto? = null,
    val connection: String? = null,
    val remoteValue: String? = null,
    val differsFromRemote: Boolean? = null,
    val remoteError: String? = null,
)
