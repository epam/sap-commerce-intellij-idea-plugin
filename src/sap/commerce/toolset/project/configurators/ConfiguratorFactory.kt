/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.project.configurators

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import sap.commerce.toolset.project.configurator.*
import sap.commerce.toolset.project.configurators.impl.DefaultContentRootConfigurator

@Service
class ConfiguratorFactory {

    val startupConfigurators
        get() = ProjectStartupConfigurator.EP.extensionList
    val preImportConfigurators
        get() = ProjectPreImportConfigurator.EP.extensionList
    val importConfigurators
        get() = ProjectImportConfigurator.EP.extensionList
    val postImportConfigurators
        get() = ProjectPostImportConfigurator.EP.extensionList
    val refreshConfigurators
        get() = ProjectRefreshConfigurator.EP.extensionList
    val facetConfigurators
        get() = ModuleFacetConfigurator.EP.extensionList

    fun getContentRootConfigurator() = service<DefaultContentRootConfigurator>()
    fun getCompilerOutputPathsConfigurator() = service<CompilerOutputPathsConfigurator>()
    fun getLibRootsConfigurator() = service<LibRootsConfigurator>()
    fun getJavadocSettingsConfigurator() = service<JavadocSettingsConfigurator>()
    fun getModuleSettingsConfigurator() = service<ModuleSettingsConfigurator>()

    companion object {
        fun getInstance() = service<ConfiguratorFactory>()
    }
}
