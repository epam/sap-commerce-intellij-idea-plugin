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
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType

class DefaultConfiguratorFactory : ConfiguratorFactory {

    override fun getFacetConfigurators() = with(mutableListOf<FacetConfigurator>()) {
        add(FacetConfigurator.yInstance)
        FacetConfigurator.springInstance?.let { add(it) }
        FacetConfigurator.kotlinInstance?.let { add(it) }
        FacetConfigurator.webInstance?.let { add(it) }
        this
    }

    override fun getSpringConfigurator(): SpringConfigurator = SpringConfigurator.instance
        ?: SpringConfigurator.dummyInstance

    override fun getContentRootConfigurator(moduleDescriptor: ModuleDescriptor): ContentRootConfigurator =
        if (shouldBeTreatedAsReadOnly(moduleDescriptor)) ContentRootConfigurator.readOnlyInstance
        else ContentRootConfigurator.instance

    override fun getModulesDependenciesConfigurator(): ModuleDependenciesConfigurator = ModuleDependenciesConfigurator.instance
    override fun getCompilerOutputPathsConfigurator(): CompilerOutputPathsConfigurator = CompilerOutputPathsConfigurator.instance
    override fun getLibRootsConfigurator(): LibRootsConfigurator = LibRootsConfigurator.instance
    override fun getGroupModuleConfigurator(): GroupModuleConfigurator = GroupModuleConfigurator.instance
    override fun getJavadocModuleConfigurator(): JavadocModuleConfigurator = JavadocModuleConfigurator.instance
    override fun getModuleSettingsConfigurator(): ModuleSettingsConfigurator = ModuleSettingsConfigurator.instance
    override fun getVersionControlSystemConfigurator(): VersionControlSystemConfigurator = VersionControlSystemConfigurator.instance
    override fun getDebugRunConfigurationConfigurator(): RunConfigurationConfigurator = RunConfigurationConfigurator.instance
    override fun getAntConfigurator(): AntConfigurator? = AntConfigurator.instance
    override fun getMavenConfigurator(): MavenConfigurator? = MavenConfigurator.instance
    override fun getEclipseConfigurator(): EclipseConfigurator? = EclipseConfigurator.instance
    override fun getGradleConfigurator(): GradleConfigurator? = GradleConfigurator.instance
    override fun getSearchScopeConfigurator(): SearchScopeConfigurator = SearchScopeConfigurator.instance
    override fun getDataSourcesConfigurator(): DataSourcesConfigurator = DataSourcesConfigurator.instance
    override fun getJavaCompilerConfigurator(): JavaCompilerConfigurator = JavaCompilerConfigurator.instance
    override fun getKotlinCompilerConfigurator(): KotlinCompilerConfigurator? = KotlinCompilerConfigurator.instance
    override fun getLoadedConfigurator(): LoadedConfigurator = LoadedConfigurator.instance

    private fun shouldBeTreatedAsReadOnly(moduleDescriptor: ModuleDescriptor) = if (moduleDescriptor.descriptorType === ModuleDescriptorType.CUSTOM) {
        false
    } else moduleDescriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode()
}
