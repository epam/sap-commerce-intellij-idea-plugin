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
 * Every declaration of the project which breaks a rule of the SAP Commerce Cloud catalogue.
 *
 * @param sources property files the chain was built from, so an empty result can be read as "nothing is wrong"
 * rather than "nothing was looked at".
 */
@Serializable
data class CxPropertyValidationDto(
    val sources: Int,
    val properties: Int,
    val errors: Int,
    val warnings: Int,
    val truncated: Boolean,
    val violations: List<CxPropertyViolationDto>,
)
