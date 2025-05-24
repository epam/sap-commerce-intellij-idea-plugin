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
package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.idea.plugin.hybris.project.configurators.impl.*
import com.intellij.idea.plugin.hybris.project.utils.Plugin
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceOrNull

@Service
class ConfiguratorFactory {

    fun getFacetConfigurators() = listOfNotNull(
        serviceOrNull<YFacetConfigurator>(),
        serviceOrNull<SpringFacetConfigurator>(),
        serviceOrNull<KotlinFacetConfigurator>(),
        serviceOrNull<WebFacetConfigurator>()
    )

    fun getSpringConfigurator() = serviceOrNull<DefaultSpringConfigurator>()
        ?: service<DummySpringConfigurator>()

    fun getContentRootConfigurator() = service<DefaultContentRootConfigurator>()
    fun getModuleDependenciesConfigurator() = service<ModuleDependenciesConfigurator>()
    fun getCompilerOutputPathsConfigurator() = service<CompilerOutputPathsConfigurator>()
    fun getLibRootsConfigurator() = service<LibRootsConfigurator>()
    fun getGroupModuleConfigurator() = service<GroupModuleConfigurator>()
    fun getJavadocSettingsConfigurator() = service<JavadocSettingsConfigurator>()
    fun getModuleSettingsConfigurator() = service<ModuleSettingsConfigurator>()
    fun getVersionControlSystemConfigurator() = service<VersionControlSystemConfigurator>()
    fun getSearchScopeConfigurator() = service<SearchScopeConfigurator>()
    fun getJavaCompilerConfigurator() = service<JavaCompilerConfigurator>()
    fun getRunConfigurationConfigurator() = service<RunConfigurationConfigurator>()

    fun getMavenConfigurator() = serviceOrNull<MavenConfigurator>()
    fun getEclipseConfigurator() = serviceOrNull<EclipseConfigurator>()
    fun getGradleConfigurator() = serviceOrNull<GradleConfigurator>()
    fun getAngularConfigurator() = serviceOrNull<AngularConfigurator>()
    fun getLoadedConfigurator() = service<LoadedConfigurator>()

    fun getAntConfigurator() = Plugin.ANT_SUPPORT.ifActive { serviceOrNull<AntConfigurator>() }

    fun getDataSourcesConfigurator() = serviceOrNull<DataSourcesConfigurator>()
    fun getJRebelConfigurator() = serviceOrNull<JRebelConfigurator>()
    fun getXsdSchemaConfigurator() = serviceOrNull<XsdSchemaConfigurator>()
    fun getKotlinCompilerConfigurator() = serviceOrNull<KotlinCompilerConfigurator>()

    companion object {
        fun getInstance() = service<ConfiguratorFactory>()
    }
}
