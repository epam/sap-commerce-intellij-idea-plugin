/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.tools.remote.http

import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

fun monitorImpexFiles(value: Int, unit: TimeUnit, pathToData: String): HybrisHttpResult {
    val resultBuilder = HybrisHttpResult.HybrisHttpResultBuilder.createResult()
    val minutesAgo = LocalDateTime.now().minusMinutes(unit.toMinutes(value.toLong()))
    val out = StringBuilder()
    File(pathToData).walk()
            .filter { file -> file.extension == "bin" }
            .filter { file -> file.lastModified().toLocalDateTime().isAfter(minutesAgo) }
            .sortedBy { it.lastModified() }
            .forEach {
                val header = "# File Path:  ${it.path}\n# file modified: ${it.lastModified().toLocalDateTime()}"
                out.append("\n#" + "-".repeat(header.length - 1) + "\n")
                out.append(header)
                out.append("\n#" + "-".repeat(header.length - 1) + "\n")
                out.append("\n${it.readText()}\n")
            }

    return resultBuilder.httpCode(200)
            .output(out.toString())
            .build()
}

private fun Long.toLocalDateTime() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
