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

    fun isDirectoryExcluded(path: Path): Boolean = path.endsWith(ProjectConstants.ExcludeDirectories.BOOTSTRAP)
        || path.endsWith(ProjectConstants.ExcludeDirectories.DATA)
        || path.endsWith(ProjectConstants.ExcludeDirectories.GRADLE)
        || path.endsWith(ProjectConstants.ExcludeDirectories.ECLIPSE_BIN)
        || path.endsWith(ProjectConstants.ExcludeDirectories.GIT)
        || path.endsWith(ProjectConstants.ExcludeDirectories.HG)
        || path.endsWith(ProjectConstants.ExcludeDirectories.SVN)
        || path.endsWith(ProjectConstants.ExcludeDirectories.GITHUB)
        || path.endsWith(ProjectConstants.ExcludeDirectories.IDEA)
        || path.endsWith(ProjectConstants.ExcludeDirectories.IDEA_MODULE_FILES)
        || path.endsWith(ProjectConstants.ExcludeDirectories.MACO_SX)
        || path.endsWith(ProjectConstants.ExcludeDirectories.LIB)
        || path.endsWith(ProjectConstants.ExcludeDirectories.LOG)
        || path.endsWith(ProjectConstants.ExcludeDirectories.RESOURCES)
        || path.endsWith(ProjectConstants.ExcludeDirectories.TEMP)
        || path.endsWith(ProjectConstants.ExcludeDirectories.TOMCAT)
        || path.endsWith(ProjectConstants.ExcludeDirectories.TOMCAT_6)
        || path.endsWith(ProjectConstants.ExcludeDirectories.TC_SERVER)
        || path.endsWith(ProjectConstants.ExcludeDirectories.TMP)
        || path.endsWith(ProjectConstants.ExcludeDirectories.ANT)
        || path.endsWith(ProjectConstants.ExcludeDirectories.CLASSES)
        || path.endsWith(ProjectConstants.ExcludeDirectories.TEST_CLASSES_DIRECTORY)
        || path.endsWith(ProjectConstants.ExcludeDirectories.SRC)
        || path.endsWith(ProjectConstants.ExcludeDirectories.GROOVY_SRC)
        || path.endsWith(ProjectConstants.ExcludeDirectories.KOTLIN_SRC)
        || path.endsWith(ProjectConstants.ExcludeDirectories.TEST_SRC)
        || path.endsWith(ProjectConstants.ExcludeDirectories.GROOVY_TEST_SRC)
        || path.endsWith(ProjectConstants.ExcludeDirectories.KOTLIN_TEST_SRC)
        || path.endsWith(ProjectConstants.ExcludeDirectories.JS_STOREFRONT)
        || path.endsWith(ProjectConstants.ExcludeDirectories.NODE_MODULES)

    companion object {
        @JvmStatic
        fun getInstance(): HybrisProjectImportService = application.service()
    }
}