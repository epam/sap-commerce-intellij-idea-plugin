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

package com.intellij.idea.plugin.hybris.tools.remote.execution.monitor

import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.apache.http.HttpStatus
import java.io.File
import java.io.Serial
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Service(Service.Level.PROJECT)
class ImpExMonitorExecutionClient(project: Project, coroutineScope: CoroutineScope) : DefaultExecutionClient<ImpExMonitorExecutionContext>(project, coroutineScope) {

    override suspend fun execute(context: ImpExMonitorExecutionContext): DefaultExecutionResult {
        val unit = context.timeOption.unit
        val duration = context.timeOption.value.toLong()
        val minutesAgo = LocalDateTime.now().minusMinutes(unit.toMinutes(duration))
        val out = StringBuilder()
        File(context.workingDir).walk()
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

        return DefaultExecutionResult(
            statusCode = HttpStatus.SC_OK,
            output = out.toString()
        )
    }

    private fun Long.toLocalDateTime() = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    companion object {
        @Serial
        private const val serialVersionUID: Long = -6318486147370249181L
    }

}