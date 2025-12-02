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

    fun isDirectoryExcluded(path: Path): Boolean = path.endsWith(ProjectConstants.Directory.PATH_BOOTSTRAP)
        || path.endsWith(ProjectConstants.Directory.DATA)
        || path.endsWith(ProjectConstants.Directory.GRADLE)
        || path.endsWith(ProjectConstants.Directory.ECLIPSE_BIN)
        || path.endsWith(ProjectConstants.Directory.GIT)
        || path.endsWith(ProjectConstants.Directory.HG)
        || path.endsWith(ProjectConstants.Directory.SVN)
        || path.endsWith(ProjectConstants.Directory.GITHUB)
        || path.endsWith(ProjectConstants.Directory.IDEA)
        || path.endsWith(ProjectConstants.Directory.IDEA_MODULE_FILES)
        || path.endsWith(ProjectConstants.Directory.MACO_SX)
        || path.endsWith(ProjectConstants.Directory.LIB)
        || path.endsWith(ProjectConstants.Directory.LOG)
        || path.endsWith(ProjectConstants.Directory.RESOURCES)
        || path.endsWith(ProjectConstants.Directory.TEMP)
        || path.endsWith(ProjectConstants.Directory.TOMCAT)
        || path.endsWith(ProjectConstants.Directory.TOMCAT_6)
        || path.endsWith(ProjectConstants.Directory.TC_SERVER)
        || path.endsWith(ProjectConstants.Directory.TMP)
        || path.endsWith(ProjectConstants.Directory.ANT)
        || path.endsWith(ProjectConstants.Directory.CLASSES)
        || path.endsWith(ProjectConstants.Directory.SRC)
        || path.endsWith(ProjectConstants.Directory.TEST_SRC)
        || path.endsWith(ProjectConstants.Directory.JS_STOREFRONT)
        || path.endsWith(ProjectConstants.Directory.NODE_MODULES)

    companion object {
        @JvmStatic
        fun getInstance(): HybrisProjectImportService = application.service()
    }
}