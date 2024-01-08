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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.idea.plugin.hybris.project.configurators.*
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor

class DefaultConfiguratorFactory : ConfiguratorFactory {

    override fun getFacetConfigurators() = with(mutableListOf<FacetConfigurator>()) {
        add(FacetConfigurator.getYInstance())
        FacetConfigurator.getSpringInstance()?.let { add(it) }
        FacetConfigurator.getKotlinInstance()?.let { add(it) }
        FacetConfigurator.getWebInstance()?.let { add(it) }
        this
    }

    override fun getSpringConfigurator(): SpringConfigurator = SpringConfigurator.getInstance()
        ?: SpringConfigurator.getDummyInstance()

    override fun getContentRootConfigurator(moduleDescriptor: ModuleDescriptor): ContentRootConfigurator = ContentRootConfigurator.instance
    override fun getModulesDependenciesConfigurator(): ModuleDependenciesConfigurator = ModuleDependenciesConfigurator.getInstance()
    override fun getCompilerOutputPathsConfigurator(): CompilerOutputPathsConfigurator = CompilerOutputPathsConfigurator.getInstance()
    override fun getLibRootsConfigurator(): LibRootsConfigurator = LibRootsConfigurator.getInstance()
    override fun getGroupModuleConfigurator(): GroupModuleConfigurator = GroupModuleConfigurator.getInstance()
    override fun getModuleSettingsConfigurator(): ModuleSettingsConfigurator = ModuleSettingsConfigurator.getInstance()
    override fun getVersionControlSystemConfigurator(): VersionControlSystemConfigurator = VersionControlSystemConfigurator.getInstance()
    override fun getDebugRunConfigurationConfigurator(): RunConfigurationConfigurator = RunConfigurationConfigurator.instance
    override fun getMavenConfigurator(): MavenConfigurator? = MavenConfigurator.getInstance()
    override fun getEclipseConfigurator(): EclipseConfigurator? = EclipseConfigurator.getInstance()
    override fun getGradleConfigurator(): GradleConfigurator? = GradleConfigurator.getInstance()
    override fun getSearchScopeConfigurator(): SearchScopeConfigurator = SearchScopeConfigurator.getInstance()
    override fun getJavaCompilerConfigurator(): JavaCompilerConfigurator = JavaCompilerConfigurator.getInstance()
    override fun getLoadedConfigurator(): LoadedConfigurator = LoadedConfigurator.getInstance()
}
