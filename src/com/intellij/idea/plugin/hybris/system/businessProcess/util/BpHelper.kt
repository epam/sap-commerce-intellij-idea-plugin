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

import kotlinx.collections.immutable.persistentMapOf

object BpHelper {
    private const val SEPARATOR = " "
    private const val DATETIME_DELIMITER = "T"

    private val dateNames = persistentMapOf(
        'Y' to "year",
        'M' to "month",
        'D' to "day"
    )

    private val timeNames = persistentMapOf(
        'H' to "hour",
        'M' to "minute",
        'S' to "second"
    )

    fun parseDuration(duration: String) = buildList {
        if (duration.contains(DATETIME_DELIMITER)) {
            val splitDuration = duration.split(DATETIME_DELIMITER)
            this.addAll(parseDateDuration(splitDuration[0]))
            this.addAll(parseTimeDuration(splitDuration[1]))
        } else {
            this.addAll(parseDateDuration(duration))
        }
    }
        .joinToString(" ")


    private fun parseDateDuration(dateDuration: String): Collection<String> {
        if (dateDuration.isEmpty()) return emptyList()
        val dateStorage = mapOf(
            'Y' to StringBuilder(),
            'M' to StringBuilder(),
            'D' to StringBuilder(),
        )
        return parseDuration(dateDuration, dateStorage, dateNames)
    }

    private fun parseTimeDuration(timeDuration: String): Collection<String> {
        if (timeDuration.isEmpty()) return emptyList()
        val timeStorage = mapOf(
            'H' to StringBuilder(),
            'M' to StringBuilder(),
            'S' to StringBuilder()
        )
        return parseDuration(timeDuration, timeStorage, timeNames)
    }

    private fun parseDuration(duration: String, durationStorage: Map<Char, StringBuilder>, durationNames: Map<Char, String>): List<String> {
        val reversedDuration = duration.reversed()
        var currentDurationToken = reversedDuration.last()
        var currentResult = durationStorage[currentDurationToken]

        for (i in reversedDuration.indices) {
            val c = reversedDuration[i]

            if (c.isDigit() || c == '.') {
                currentResult?.insert(0, c)
            }

            if (c.isLetter() || i == reversedDuration.length - 1) {
                if (c == currentDurationToken) continue
                else {
                    val tokenStorage = durationStorage[currentDurationToken]
                    if (tokenStorage.contentEquals("0")) {
                        tokenStorage?.setLength(0)
                    } else {
                        val postfix = if (tokenStorage.contentEquals("1")) "" else "s"
                        tokenStorage?.append(" ${durationNames[currentDurationToken]}$postfix")
                    }
                }
                currentDurationToken = c
                currentResult = durationStorage[c]
            }

        }
        return durationStorage.values
            .filter { it.isNotBlank() }
            .map { it.toString() }
    }
}