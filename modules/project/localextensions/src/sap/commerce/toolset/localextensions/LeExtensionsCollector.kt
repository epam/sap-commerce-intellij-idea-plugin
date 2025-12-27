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

package sap.commerce.toolset.localextensions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.PropertiesUtil
import com.intellij.util.application
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.localextensions.jaxb.Hybrisconfig
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.max

@Service
class LeExtensionsCollector {

    fun collect(
        foundExtensions: Collection<LeExtension>,
        configDirectory: File,
        platformDirectory: File?
    ): Set<String> = ApplicationManager.getApplication().runReadAction(Computable {
        val hybrisConfig = LeUnmarshaller.getInstance().unmarshal(configDirectory)
            ?: return@Computable setOf()

        TreeSet(String.CASE_INSENSITIVE_ORDER).apply {
            addAll(processExtensions(hybrisConfig))
            addAll(processAutoloadPaths(foundExtensions, hybrisConfig, platformDirectory))
        }
    })

    private fun processAutoloadPaths(foundExtensions: Collection<LeExtension>, hybrisConfig: Hybrisconfig, platformDirectory: File?): Collection<String> {
        platformDirectory ?: return emptySet()
        val extensionNames = mutableSetOf<String>()
        val autoloadPaths = HashMap<String, Int>()

        hybrisConfig.getExtensions().getPath()
            .filter { it.isAutoload }
            .filter { it.dir != null }
            .forEach {
                val depth = it.depth
                val dir = it.dir!!

                if (depth == null) autoloadPaths[dir] = HybrisConstants.DEFAULT_EXTENSIONS_PATH_DEPTH
                else if (depth > 1) {
                    if (!autoloadPaths.containsKey(dir)) autoloadPaths[dir] = depth
                    else autoloadPaths.computeIfPresent(dir) { _: String, oldValue: Int ->
                        max(oldValue, depth)
                    }
                }
            }

        if (autoloadPaths.isEmpty()) return emptySet()

        val platform = Paths.get(platformDirectory.path, HybrisConstants.PLATFORM_MODULE_PREFIX).toString()
        val path = Paths.get(platform, "env.properties")

        try {
            Files.newBufferedReader(path, StandardCharsets.ISO_8859_1).use { fis ->
                val properties = PropertiesUtil.loadProperties(fis)

                properties.entries.forEach {
                    val value = it.value.replace("\${platformhome}", platform)
                    it.setValue(Paths.get(value).normalize().toString())
                }
                properties["platformhome"] = platform

                val normalizedPaths = autoloadPaths.entries
                    .associate { entry ->
                        val key = properties.entries
                            .filter { property -> entry.key.contains("\${" + property.key + '}') }
                            .firstNotNullOfOrNull { property -> entry.key.replace("\${" + property.key + '}', property.value) }
                            ?: entry.key
                        val normalizedKey = Paths.get(key).normalize().toString()

                        normalizedKey to entry.value
                    }

                foundExtensions.forEach {
                    for (entry in normalizedPaths.entries) {
                        val moduleDir = it.directory.path
                        if (moduleDir.startsWith(entry.key)
                            && Paths.get(moduleDir.substring(entry.key.length)).nameCount <= entry.value
                        ) {
                            extensionNames.add(it.name)
                            break
                        }
                    }
                }
            }
        } catch (_: IOException) {
            // NOP
        }

        return extensionNames;
    }

    private fun processExtensions(hybrisConfig: Hybrisconfig) = buildList {
        for (extensionType in hybrisConfig.getExtensions().getExtension()) {
            val name = extensionType.getName()
            if (name != null) {
                add(name)
                continue
            }
            val dir = extensionType.getDir()

            if (dir == null) continue

            val indexSlash = dir.lastIndexOf('/')
            val indexBack = dir.lastIndexOf('\\')
            val index = max(indexSlash, indexBack)
            if (index == -1) {
                add(dir)
            } else {
                add(dir.substring(index + 1))
            }
        }
    }

    companion object {
        fun getInstance(): LeExtensionsCollector = application.service()
    }
}
