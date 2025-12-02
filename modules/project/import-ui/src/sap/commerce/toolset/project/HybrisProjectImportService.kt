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

    private val excludedDirectories = setOf(
        ProjectConstants.Directory.DATA,
        ProjectConstants.Directory.GRADLE,
        ProjectConstants.Directory.ECLIPSE_BIN,
        ProjectConstants.Directory.GIT,
        ProjectConstants.Directory.HG,
        ProjectConstants.Directory.SVN,
        ProjectConstants.Directory.GITHUB,
        ProjectConstants.Directory.ANGULAR,
        ProjectConstants.Directory.SETTINGS,
        ProjectConstants.Directory.IDEA,
        ProjectConstants.Directory.IDEA_MODULE_FILES,
        ProjectConstants.Directory.MACO_SX,
        ProjectConstants.Directory.LIB,
        ProjectConstants.Directory.LOG,
        ProjectConstants.Directory.RESOURCES,
        ProjectConstants.Directory.TOMCAT,
        ProjectConstants.Directory.TOMCAT_6,
        ProjectConstants.Directory.TC_SERVER,
        ProjectConstants.Directory.TEMP,
        ProjectConstants.Directory.TMP,
        ProjectConstants.Directory.ANT,
        ProjectConstants.Directory.CLASSES,
        ProjectConstants.Directory.TEST_CLASSES,
        ProjectConstants.Directory.SRC,
        ProjectConstants.Directory.GROOVY_SRC,
        ProjectConstants.Directory.KOTLIN_SRC,
        ProjectConstants.Directory.SCALA_SRC,
        ProjectConstants.Directory.TEST_SRC,
        ProjectConstants.Directory.GROOVY_TEST_SRC,
        ProjectConstants.Directory.KOTLIN_TEST_SRC,
        ProjectConstants.Directory.SCALA_TEST_SRC,
        ProjectConstants.Directory.NODE_MODULES,
    )

    fun isDirectoryExcluded(path: Path): Boolean = excludedDirectories.contains(path.name)
        || path.endsWith(ProjectConstants.Directory.PATH_BOOTSTRAP)

    companion object {
        @JvmStatic
        fun getInstance(): HybrisProjectImportService = application.service()
    }
}