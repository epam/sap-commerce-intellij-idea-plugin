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

package sap.commerce.toolset.project

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import java.nio.file.Path
import kotlin.io.path.name

@Service
class HybrisProjectImportService {

    private val excludedDirectories = buildSet {
        add(ProjectConstants.Directory.DATA)
        add(ProjectConstants.Directory.GRADLE)
        add(ProjectConstants.Directory.ECLIPSE_BIN)
        add(ProjectConstants.Directory.GIT)
        add(ProjectConstants.Directory.HG)
        add(ProjectConstants.Directory.SVN)
        add(ProjectConstants.Directory.GITHUB)
        add(ProjectConstants.Directory.ANGULAR)
        add(ProjectConstants.Directory.SETTINGS)
        add(ProjectConstants.Directory.IDEA)
        add(ProjectConstants.Directory.IDEA_MODULE_FILES)
        add(ProjectConstants.Directory.MACO_SX)
        add(ProjectConstants.Directory.LIB)
        add(ProjectConstants.Directory.LOG)
        add(ProjectConstants.Directory.RESOURCES)
        add(ProjectConstants.Directory.TOMCAT)
        add(ProjectConstants.Directory.TOMCAT_6)
        add(ProjectConstants.Directory.TC_SERVER)
        add(ProjectConstants.Directory.TEMP)
        add(ProjectConstants.Directory.TMP)
        add(ProjectConstants.Directory.ANT)
        add(ProjectConstants.Directory.CLASSES)
        add(ProjectConstants.Directory.TEST_CLASSES)
        add(ProjectConstants.Directory.NODE_MODULES)

        addAll(ProjectConstants.Directory.SRC_DIR_NAMES)
        addAll(ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
    }

    fun isDirectoryExcluded(path: Path): Boolean = excludedDirectories.contains(path.name)
        || path.endsWith(ProjectConstants.Directory.PATH_PLATFORM_BOOTSTRAP)

    companion object {
        @JvmStatic
        fun getInstance(): HybrisProjectImportService = application.service()
    }
}