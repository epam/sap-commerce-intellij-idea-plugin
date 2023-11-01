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

package com.intellij.idea.plugin.hybris.project.configurators;

import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ConfiguratorFactory {

    @NotNull
    List<FacetConfigurator> getFacetConfigurators();

    @NotNull
    SpringConfigurator getSpringConfigurator();

    @Nullable
    XsdSchemaConfigurator getXsdSchemaConfigurator();

    @NotNull
    ModuleDependenciesConfigurator getModulesDependenciesConfigurator();

    @NotNull
    CompilerOutputPathsConfigurator getCompilerOutputPathsConfigurator();

    @NotNull
    ContentRootConfigurator getContentRootConfigurator(final ModuleDescriptor moduleDescriptor);

    @NotNull
    LibRootsConfigurator getLibRootsConfigurator();

    @NotNull
    GroupModuleConfigurator getGroupModuleConfigurator();

    @NotNull
    JavadocModuleConfigurator getJavadocModuleConfigurator();

    @NotNull
    ModuleSettingsConfigurator getModuleSettingsConfigurator();

    @NotNull
    VersionControlSystemConfigurator getVersionControlSystemConfigurator();

    @NotNull
    RunConfigurationConfigurator getDebugRunConfigurationConfigurator();

    @Nullable
    AntConfigurator getAntConfigurator();

    @Nullable
    MavenConfigurator getMavenConfigurator();

    @Nullable
    EclipseConfigurator getEclipseConfigurator();

    @NotNull
    SearchScopeConfigurator getSearchScopeConfigurator();

    @NotNull
    LoadedConfigurator getLoadedConfigurator();

    @Nullable
    GradleConfigurator getGradleConfigurator();

    @Nullable
    DataSourcesConfigurator getDataSourcesConfigurator();

    @Nullable
    JavaCompilerConfigurator getJavaCompilerConfigurator();

    @Nullable
    KotlinCompilerConfigurator getKotlinCompilerConfigurator();
}
