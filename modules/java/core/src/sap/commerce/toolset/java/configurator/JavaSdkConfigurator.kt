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
package sap.commerce.toolset.java.configurator

import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.roots.LanguageLevelProjectExtension
import com.intellij.openapi.roots.ProjectRootManager
import sap.commerce.toolset.project.configurator.ProjectPreImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor

class JavaSdkConfigurator : ProjectPreImportConfigurator {

    override val name: String
        get() = "Java Sdk"

    override fun preConfigure(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return
        val projectRootManager = ProjectRootManager.getInstance(project)

        val projectSdk = projectRootManager.projectSdk ?: return
        val versionString = projectSdk.versionString ?: return
        val sdkVersion = JavaSdkVersion.fromVersionString(versionString) ?: return
        val languageLevelExt = LanguageLevelProjectExtension.getInstance(project)

        if (sdkVersion.maxLanguageLevel != languageLevelExt.languageLevel) {
            languageLevelExt.languageLevel = sdkVersion.maxLanguageLevel
        }
    }
}
