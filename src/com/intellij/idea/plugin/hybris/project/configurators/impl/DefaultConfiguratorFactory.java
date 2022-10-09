/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.idea.plugin.hybris.project.configurators.AntConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.CompilerOutputPathsConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.ConfiguratorFactory;
import com.intellij.idea.plugin.hybris.project.configurators.ContentRootConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.DataSourcesConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.EclipseConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.FacetConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.GradleConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.GroupModuleConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.JavaCompilerConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.JavadocModuleConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.LibRootsConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.LoadedConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.MavenConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.ModuleSettingsConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.ModulesDependenciesConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.RunConfigurationConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.SearchScopeConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.SpringConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.TestRunConfigurationConfigurator;
import com.intellij.idea.plugin.hybris.project.configurators.VersionControlSystemConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Zdarsky (martin.zdarsky@hybris.com) on 18/08/15.
 */
public class DefaultConfiguratorFactory implements ConfiguratorFactory {

    @NotNull
    @Override
    public List<FacetConfigurator> getFacetConfigurators() {
        final FacetConfigurator springFacetConfigurator = ApplicationManager.getApplication().getService(SpringFacetConfigurator.class);
        final FacetConfigurator webFacetConfigurator = ApplicationManager.getApplication().getService(WebFacetConfigurator.class);

        final List<FacetConfigurator> facetConfigurators = new ArrayList<FacetConfigurator>(2);

        if (null != springFacetConfigurator) {
            facetConfigurators.add(springFacetConfigurator);
        }

        if (null != webFacetConfigurator) {
            facetConfigurators.add(webFacetConfigurator);
        }

        return facetConfigurators;
    }

    @NotNull
    @Override
    public SpringConfigurator getSpringConfigurator() {
        final SpringConfigurator springConfigurator = ApplicationManager.getApplication().getService(DefaultSpringConfigurator.class);

        return (null == springConfigurator) ? new DummySpringConfigurator() : springConfigurator;
    }

    @NotNull
    @Override
    public ModulesDependenciesConfigurator getModulesDependenciesConfigurator() {
        return ApplicationManager.getApplication().getService(ModulesDependenciesConfigurator.class);
    }

    @NotNull
    @Override
    public CompilerOutputPathsConfigurator getCompilerOutputPathsConfigurator() {
        return ApplicationManager.getApplication().getService(CompilerOutputPathsConfigurator.class);
    }

    @NotNull
    @Override
    public ContentRootConfigurator getRegularContentRootConfigurator() {
        return ApplicationManager.getApplication().getService(RegularContentRootConfigurator.class);
    }

    @NotNull
    @Override
    public ContentRootConfigurator getReadOnlyContentRootConfigurator() {
        return ApplicationManager.getApplication().getService(ReadOnlyContentRootConfigurator.class);
    }

    @NotNull
    @Override
    public LibRootsConfigurator getLibRootsConfigurator() {
        return ApplicationManager.getApplication().getService(LibRootsConfigurator.class);
    }

    @NotNull
    @Override
    public GroupModuleConfigurator getGroupModuleConfigurator() {
        return ApplicationManager.getApplication().getService(GroupModuleConfigurator.class);
    }

    @NotNull
    @Override
    public JavadocModuleConfigurator getJavadocModuleConfigurator() {
        return ApplicationManager.getApplication().getService(JavadocModuleConfigurator.class);
    }

    @NotNull
    @Override
    public ModuleSettingsConfigurator getModuleSettingsConfigurator() {
        return ApplicationManager.getApplication().getService(ModuleSettingsConfigurator.class);
    }

    @NotNull
    @Override
    public VersionControlSystemConfigurator getVersionControlSystemConfigurator() {
        return ApplicationManager.getApplication().getService(VersionControlSystemConfigurator.class);
    }

    @NotNull
    @Override
    public RunConfigurationConfigurator getDebugRunConfigurationConfigurator() {
        return ApplicationManager.getApplication().getService(DebugRunConfigurationConfigurator.class);
    }

    @Nullable
    @Override
    public RunConfigurationConfigurator getTestRunConfigurationConfigurator() {
        return ApplicationManager.getApplication().getService(TestRunConfigurationConfigurator.class);
    }

    @Nullable
    @Override
    public AntConfigurator getAntConfigurator() {
        return ApplicationManager.getApplication().getService(AntConfigurator.class);
    }

    @Nullable
    @Override
    public MavenConfigurator getMavenConfigurator() {
        return ApplicationManager.getApplication().getService(MavenConfigurator.class);
    }

    @Nullable
    @Override
    public EclipseConfigurator getEclipseConfigurator() {
        return ApplicationManager.getApplication().getService(EclipseConfigurator.class);
    }

    @Nullable
    @Override
    public GradleConfigurator getGradleConfigurator() {
        return ApplicationManager.getApplication().getService(GradleConfigurator.class);
    }

    @NotNull
    @Override
    public SearchScopeConfigurator getSearchScopeConfigurator() {
        return ApplicationManager.getApplication().getService(SearchScopeConfigurator.class);
    }

    @Nullable
    @Override
    public DataSourcesConfigurator getDataSourcesConfigurator() {
        return ApplicationManager.getApplication().getService(DataSourcesConfigurator.class);
    }

    @Nullable
    @Override
    public JavaCompilerConfigurator getCompilerConfigurator() {
        return ApplicationManager.getApplication().getService(JavaCompilerConfigurator.class);
    }

    @NotNull
    @Override
    public LoadedConfigurator getLoadedConfigurator() {
        return ApplicationManager.getApplication().getService(LoadedConfigurator.class);
    }

    protected static class DummySpringConfigurator implements SpringConfigurator {

        @Override
        public void findSpringConfiguration(@NotNull final List<HybrisModuleDescriptor> modulesChosenForImport) {

        }

        @Override
        public void configureDependencies(
            @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
            @NotNull final IdeModifiableModelsProvider modifiableModelsProvider
        ) {

        }
    }
}
