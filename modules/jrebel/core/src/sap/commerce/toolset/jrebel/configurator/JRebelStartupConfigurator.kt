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
package sap.commerce.toolset.jrebel.configurator

import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.toNioPathOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.configurator.ProjectStartupConfigurator
import sap.commerce.toolset.project.facet.YFacet
import sap.commerce.toolset.util.fileExists
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class JRebelStartupConfigurator : ProjectStartupConfigurator {

    override val name: String
        get() = "JRebel"

    override suspend fun onStartup(project: Project) {
        val module = ModuleManager.getInstance(project).modules
            .find {
                YFacet.getState(it)?.name == EiConstants.Extension.PLATFORM
            }
            ?: return

        val compilingXml = readAction {
            ModuleRootManager.getInstance(module)
                .contentRoots
                .firstNotNullOfOrNull { root ->
                    root.findFileByRelativePath(HybrisConstants.ANT_COMPILING_XML)
                }
        }
            ?.toNioPathOrNull()
            ?.takeIf { it.fileExists }
            ?: return

        try {
            var shouldUpdate = false
            val updatedLines = withContext(Dispatchers.IO) {
                Files.readAllLines(compilingXml, Charsets.UTF_8)
            }
                .map { line ->
                    if ("excludes=\"**/rebel.xml\"" in line) {
                        shouldUpdate = true
                        line.replace(
                            "excludes=\"**/rebel.xml\"",
                            ""
                        )
                    } else line
                }

            if (shouldUpdate) withContext(Dispatchers.IO) {
                Files.write(
                    compilingXml,
                    updatedLines,
                    Charsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            }
        } catch (e: Exception) {
            thisLogger().error(e)
        }
    }
}
