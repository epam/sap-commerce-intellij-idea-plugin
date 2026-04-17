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

package sap.commerce.toolset.localextensions

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.PropertiesUtil
import com.intellij.util.application
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.localextensions.jaxb.Hybrisconfig
import sap.commerce.toolset.util.directoryExists
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.io.path.pathString

@Service
class LeExtensionsCollector {

    suspend fun collect(
        foundExtensions: Collection<LeExtension>,
        configDirectory: Path,
        platformDirectory: Path
    ): Map<String, LeExtension> = readAction {
        val hybrisConfig = LeUnmarshaller.getInstance().unmarshal(configDirectory)
            ?: return@readAction mapOf()
        val expandedProperties = getExpandedProperties(platformDirectory)
            ?: return@readAction mapOf()
        val scanTypes = getScanTypes(hybrisConfig, expandedProperties)
            ?: run {
                thisLogger().warn("No scan types defined in the localextensions.xml.")
                return@readAction mapOf()
            }

        buildMap {
            // declared via `path autoload="true"`
            processAutoloadPaths(foundExtensions, scanTypes).forEach { leExtension ->
                put(leExtension.name, leExtension)
            }

            // declared via:
            // 1. `extension name="myextension" dir="someDir"`
            // 2. `extension name="myextension"`
            processExtensions(hybrisConfig, scanTypes, expandedProperties).forEach { leExtension ->
                if (!this.contains(leExtension.name)) {
                    put(leExtension.name, leExtension)
                }
            }
        }
    }

    private fun getScanTypes(
        hybrisConfig: Hybrisconfig,
        expandedProperties: Map<String, String>
    ): Map<String, ScanType>? {
        val scanTypes = mutableMapOf<String, ScanType>()

        hybrisConfig.getExtensions().getPath().forEach { scanType ->
            val dir = scanType.dir ?: return@forEach
            val depth = scanType.depth ?: HybrisConstants.DEFAULT_EXTENSIONS_PATH_DEPTH

            scanTypes.getOrPut(dir) {
                ScanType(
                    dir = dir,
                    autoload = scanType.isAutoload,
                    depth = HybrisConstants.DEFAULT_EXTENSIONS_PATH_DEPTH,
                    normalizedPath = dir.toNormalizedPath(expandedProperties),
                )
            }.apply {
                if (this.depth < depth) {
                    this.depth = depth
                }
            }
        }

        if (scanTypes.isEmpty()) return null

        scanTypes.entries.forEach { (dir, scanType) ->
            scanType.normalizedPath = dir.toNormalizedPath(expandedProperties)
        }

        return scanTypes
    }

    private fun processAutoloadPaths(
        foundExtensions: Collection<LeExtension>,
        scanTypes: Map<String, ScanType>,
    ): Collection<LeExtension> = scanTypes.values
        .filter { it.autoload }
        .takeIf { it.isNotEmpty() }
        ?.let { autoloadScanTypes ->
            foundExtensions.filter { extension ->
                autoloadScanTypes
                    .firstOrNull { scanType ->
                        val moduleDir = extension.directory.normalize().pathString
                        moduleDir.startsWith(scanType.normalizedPath.pathString)
                            && Paths.get(moduleDir.substring(scanType.dir.length)).nameCount <= scanType.depth
                    } != null
            }
        }
        ?: emptyList()

    private fun processExtensions(
        hybrisConfig: Hybrisconfig,
        scanTypes: Map<String, ScanType>,
        expandedProperties: Map<String, String>,
    ) = hybrisConfig.getExtensions().getExtension()
        .mapNotNull { extensionType ->
            val extensionName = extensionType.name.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            val extensionPath = extensionType.dir
                .takeIf { it.isNotBlank() }
                ?.toNormalizedPath(expandedProperties)
                ?: scanTypes.values
                    .firstNotNullOfOrNull { it.findExtensionDirectory(extensionName) }
                ?: return@mapNotNull null

            LeExtension(extensionName, extensionPath)
        }

    private fun getExpandedProperties(platformDirectory: Path): Map<String, String>? {
        val platformPath = platformDirectory.resolve("bin").resolve("platform")
        val envPropertiesPath = platformPath.resolve("env.properties")

        return runCatching {
            Files.newBufferedReader(envPropertiesPath, StandardCharsets.ISO_8859_1).use { fis ->
                val properties = PropertiesUtil.loadProperties(fis)

                properties.entries.forEach {
                    val value = it.value.replace("\${platformhome}", platformPath.pathString)
                    it.setValue(Paths.get(value).normalize().toString())
                }
                properties.apply {
                    this["platformhome"] = platformPath.pathString
                }
            }
        }.getOrNull()
    }

    fun ScanType.findExtensionDirectory(
        targetName: String,
    ): Path? = Files.walk(this.normalizedPath, this.depth)
        .filter { it.directoryExists && it.name == targetName }
        .findFirst()
        .orElse(null)

    private fun String.toNormalizedPath(expandedProperties: Map<String, String>): Path {
        val key = expandedProperties.entries
            .filter { property -> contains("\${" + property.key + '}') }
            .firstNotNullOfOrNull { property -> replace("\${" + property.key + '}', property.value) }
            ?: this
        return Paths.get(key).normalize()
    }

    companion object {
        fun getInstance(): LeExtensionsCollector = application.service()
    }
}
