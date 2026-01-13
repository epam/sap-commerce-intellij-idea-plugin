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

package sap.commerce.toolset.project.context

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

// platformVersion = 2211.42 (with patch)
// platformApiVersion = 2211
fun Path.findSourceCodeFile(platformVersion: String?, platformApiVersion: String?): Path? = Files.newDirectoryStream(this) { child ->
    child.isExtensionSupported() && (child.containsVersion(platformVersion) || child.containsVersion(platformApiVersion))
}.use { stream ->
    stream.sortedWith(
        compareBy<Path> {
            val name = it.nameWithoutExtension
            when {
                platformVersion != null && platformVersion in name -> 0
                platformApiVersion != null && platformApiVersion in name -> 1
                else -> 2
            }
        }
    ).firstOrNull()
}

private fun Path.containsVersion(version: String?): Boolean = version != null && nameWithoutExtension.contains(version)
private fun Path.isExtensionSupported(): Boolean = extension.equals("zip", true)
