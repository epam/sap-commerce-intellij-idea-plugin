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

package sap.commerce.toolset.project.welcomescreen.presentation

import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.openapi.util.io.FileUtil
import java.nio.file.Path
import javax.swing.Icon

data class SapCommerceProject(
    private val location: String,
    val displayName: String,
    val projectName: String,
    val projectIcon: Icon
) {
    val path: Path
        get() = Path.of(location)

    val locationRelativeToUserHome: String
        get() = FileUtil.getLocationRelativeToUserHome(location)

    companion object {
        fun of(location: String): SapCommerceProject {
            val manager = RecentProjectsManagerBase.getInstanceEx()
            val projectName = manager.getProjectName(location)
            val displayName = manager.getDisplayName(location) ?: projectName
            val icon = manager.getProjectIcon(location, true)
            return SapCommerceProject(location, displayName, projectName, icon)
        }
    }
}