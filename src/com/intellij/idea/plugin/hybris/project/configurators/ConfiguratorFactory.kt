/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

@Service
class ConfiguratorFactory {

    fun getFacetConfigurators() = with(ApplicationManager.getApplication()) {
        listOfNotNull(
            getService(YFacetConfigurator::class.java),
            getService(SpringFacetConfigurator::class.java),
            getService(KotlinFacetConfigurator::class.java),
            getService(WebFacetConfigurator::class.java)
        )
    }

    fun getSpringConfigurator(): SpringConfigurator = ApplicationManager.getApplication().getService(DefaultSpringConfigurator::class.java)
        ?: ApplicationManager.getApplication().getService(DummySpringConfigurator::class.java)

    fun getContentRootConfigurator(): ContentRootConfigurator = ApplicationManager.getApplication().getService(DefaultContentRootConfigurator::class.java)
    fun getModuleDependenciesConfigurator(): ModuleDependenciesConfigurator = ApplicationManager.getApplication().getService(ModuleDependenciesConfigurator::class.java)
    fun getCompilerOutputPathsConfigurator(): CompilerOutputPathsConfigurator = ApplicationManager.getApplication().getService(CompilerOutputPathsConfigurator::class.java)
    fun getLibRootsConfigurator(): LibRootsConfigurator = ApplicationManager.getApplication().getService(LibRootsConfigurator::class.java)
    fun getGroupModuleConfigurator(): GroupModuleConfigurator = ApplicationManager.getApplication().getService(GroupModuleConfigurator::class.java)
    fun getJavadocSettingsConfigurator(): JavadocSettingsConfigurator = ApplicationManager.getApplication().getService(JavadocSettingsConfigurator::class.java)
    fun getModuleSettingsConfigurator(): ModuleSettingsConfigurator = ApplicationManager.getApplication().getService(ModuleSettingsConfigurator::class.java)
    fun getVersionControlSystemConfigurator(): VersionControlSystemConfigurator = ApplicationManager.getApplication().getService(VersionControlSystemConfigurator::class.java)
    fun getSearchScopeConfigurator(): SearchScopeConfigurator = ApplicationManager.getApplication().getService(SearchScopeConfigurator::class.java)
    fun getJavaCompilerConfigurator(): JavaCompilerConfigurator = ApplicationManager.getApplication().getService(JavaCompilerConfigurator::class.java)
    fun getRunConfigurationConfigurator(): RunConfigurationConfigurator = ApplicationManager.getApplication().getService(DefaultRunConfigurationConfigurator::class.java)

    fun getMavenConfigurator(): MavenConfigurator? = ApplicationManager.getApplication().getService(MavenConfigurator::class.java)
    fun getEclipseConfigurator(): EclipseConfigurator? = ApplicationManager.getApplication().getService(EclipseConfigurator::class.java)
    fun getGradleConfigurator(): GradleConfigurator? = ApplicationManager.getApplication().getService(GradleConfigurator::class.java)
    fun getLoadedConfigurator(): LoadedConfigurator = ApplicationManager.getApplication().getService(LoadedConfigurator::class.java)

    fun getAntConfigurator(): AntConfigurator? =
        if (PluginCommon.isPluginActive(PluginCommon.PLUGIN_ANT_SUPPORT)) ApplicationManager.getApplication().getService(AntConfigurator::class.java)
        else null

    fun getDataSourcesConfigurator(): DataSourcesConfigurator? = ApplicationManager.getApplication().getService(DataSourcesConfigurator::class.java)
    fun getJRebelConfigurator(): JRebelConfigurator? = ApplicationManager.getApplication().getService(JRebelConfigurator::class.java)
    fun getXsdSchemaConfigurator(): XsdSchemaConfigurator? = ApplicationManager.getApplication().getService(XsdSchemaConfigurator::class.java)
    fun getKotlinCompilerConfigurator(): KotlinCompilerConfigurator? = ApplicationManager.getApplication().getService(KotlinCompilerConfigurator::class.java)

    companion object {
        fun getInstance(): ConfiguratorFactory = ApplicationManager.getApplication().getService(ConfiguratorFactory::class.java)
    }
}
