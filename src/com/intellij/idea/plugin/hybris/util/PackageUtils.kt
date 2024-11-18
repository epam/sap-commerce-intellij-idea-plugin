/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.util

object PackageUtils {

    fun abbreviatePackageName(fullName: String): String {
        val parts = fullName.split(".")
        if (parts.isEmpty()) return fullName

        val lastPart = parts.last()
        val isClassName = lastPart.firstOrNull()?.isUpperCase() == true

        return if (isClassName) {
            parts.dropLast(1).joinToString(".") { it.firstOrNull()?.toString() ?: "" } + ".$lastPart"
        } else {
            val numOfLastSegments = when (parts.size) {
                in 1..3 -> 1
                in 4..5 -> parts.size % 3
                else -> 3
            }

            val abbreviatedParts = parts.dropLast(numOfLastSegments)
                .map { it.firstOrNull()?.toString() ?: "" }
            val lastThreeSegments = parts.takeLast(numOfLastSegments)

            (abbreviatedParts + lastThreeSegments).joinToString(".")
        }
    }
}