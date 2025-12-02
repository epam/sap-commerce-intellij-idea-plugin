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

@Service
class HybrisProjectImportService {

    fun isDirectoryExcluded(path: Path): Boolean = path.endsWith(ProjectConstants.Directories.PATH_BOOTSTRAP)
        || path.endsWith(ProjectConstants.Directories.DATA)
        || path.endsWith(ProjectConstants.Directories.GRADLE)
        || path.endsWith(ProjectConstants.Directories.ECLIPSE_BIN)
        || path.endsWith(ProjectConstants.Directories.GIT)
        || path.endsWith(ProjectConstants.Directories.HG)
        || path.endsWith(ProjectConstants.Directories.SVN)
        || path.endsWith(ProjectConstants.Directories.GITHUB)
        || path.endsWith(ProjectConstants.Directories.IDEA)
        || path.endsWith(ProjectConstants.Directories.IDEA_MODULE_FILES)
        || path.endsWith(ProjectConstants.Directories.MACO_SX)
        || path.endsWith(ProjectConstants.Directories.LIB)
        || path.endsWith(ProjectConstants.Directories.LOG)
        || path.endsWith(ProjectConstants.Directories.RESOURCES)
        || path.endsWith(ProjectConstants.Directories.TEMP)
        || path.endsWith(ProjectConstants.Directories.TOMCAT)
        || path.endsWith(ProjectConstants.Directories.TOMCAT_6)
        || path.endsWith(ProjectConstants.Directories.TC_SERVER)
        || path.endsWith(ProjectConstants.Directories.TMP)
        || path.endsWith(ProjectConstants.Directories.ANT)
        || path.endsWith(ProjectConstants.Directories.CLASSES)
        || path.endsWith(ProjectConstants.Directories.SRC)
        || path.endsWith(ProjectConstants.Directories.TEST_SRC)
        || path.endsWith(ProjectConstants.Directories.JS_STOREFRONT)
        || path.endsWith(ProjectConstants.Directories.NODE_MODULES)

    companion object {
        @JvmStatic
        fun getInstance(): HybrisProjectImportService = application.service()
    }
}