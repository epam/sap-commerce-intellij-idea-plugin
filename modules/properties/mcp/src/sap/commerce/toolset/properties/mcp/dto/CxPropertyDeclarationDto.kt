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
 * One declaration of a property, and what the chain makes of it.
 *
 * @param wins whether this is the declaration the platform applies.
 * @param ignored whether the running system reads this file at all — an extension `localextensions.xml` does not
 * list is imported by the project but never loaded, so its declarations lose to every active one.
 */
@Serializable
data class CxPropertyDeclarationDto(
    val value: String,
    val file: String,
    val scope: String,
    val wins: Boolean,
    val ignored: Boolean,
    val extension: String? = null,
    val path: String? = null,
)
