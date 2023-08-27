/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.businessProcess.util

object BpHelper {

    val timeNames = mapOf(
        'Y' to "year",
        'M' to "month",
        'D' to "day",
        'H' to "hour",
        'M' to "month",
        'S' to "second"
    )

    fun parseDuration(duration: String): String {
        val map = mapOf(
            'S' to StringBuilder(),
            'M' to StringBuilder(),
            'H' to StringBuilder(),
            'D' to StringBuilder(),
            'M' to StringBuilder(),
            'Y' to StringBuilder()
        )
        val reversedDuration = duration.reversed()
        var currentTimeToken = reversedDuration.last()
        var currentResult = map[currentTimeToken]

        for (i in reversedDuration.indices) {
            val c = reversedDuration[i]

            if (c.isLetter()) {
                if (c == currentTimeToken) continue
                else {
                    val time = map[currentTimeToken]
                    val postfix = if (time.contentEquals("1")) "" else "s"
                    time?.append(" ${timeNames[currentTimeToken]}$postfix")
                }
                currentTimeToken = c
                currentResult = map[c]
            }
            if (c.isDigit()) {
                currentResult?.insert(0, c)
            }
        }
        return map.map { it.value }
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .takeIf { it.isNotBlank() }
            ?: "?"
    }
}