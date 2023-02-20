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

import com.intellij.idea.plugin.hybris.project.configurators.ConfiguratorFactory;
import com.intellij.idea.plugin.hybris.project.configurators.MavenConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.MavenModuleDescriptor;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.impl.ModuleEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenImportListener;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.wizards.MavenProjectBuilder;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.project.utils.ModuleGroupUtils.fetchGroupMapping;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 13/11/16.
 */
public class DefaultMavenConfigurator implements MavenConfigurator {

    private HybrisMavenImportListener mavenImportListener;

    @Override
    public void configure(
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final Project project,
        @NotNull final List<MavenModuleDescriptor> mavenModules,
        @NotNull final ConfiguratorFactory configuratorFactory
    ) {
        final List<VirtualFile> mavenProjectFiles = mavenModules
            .stream()
            .map(e -> new File(e.getRootDirectory(), MavenConstants.POM_XML))
            .map(e -> VfsUtil.findFileByIoFile(e, true))
            .collect(Collectors.toList());

        final var mavenProjectBuilders = mavenProjectFiles.stream()
                                                          .map(mavenProjectBuilderFunction(project))
                                                          .filter(isProjectPathValid(mavenModules))
                                                          .toList();

        if (mavenImportListener == null) {
            mavenImportListener = new HybrisMavenImportListener(project);
            project.getMessageBus().connect().subscribe(MavenImportListener.TOPIC, mavenImportListener);
        }
        mavenImportListener.setMavenModulesConfig(mavenProjectFiles, mavenModules, configuratorFactory);

        mavenProjectBuilders.forEach(builder -> {
            try {
                builder.commit(project, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
            } finally {
                builder.cleanup();
            }
        });

        MavenProjectsManager.getInstance(project).importProjects();
    }

    @NotNull
    private Function<VirtualFile, MavenProjectBuilder> mavenProjectBuilderFunction(final @NotNull Project project) {
        return mavenProjectFile -> {
            final var builder = new MavenProjectBuilder();
            builder.setUpdate(MavenProjectsManager.getInstance(project).isMavenizedProject());
            builder.setFileToImport(mavenProjectFile);
            return builder;
        };
    }

    @NotNull
    private Predicate<MavenProjectBuilder> isProjectPathValid(final @NotNull List<MavenModuleDescriptor> mavenModules) {
        return builder -> {
            final var path = builder.getRootPath()
                                    .toAbsolutePath()
                                    .toString();
            return mavenModules.stream()
                               .anyMatch(module -> module.getRootDirectory()
                                                         .getAbsolutePath()
                                                         .equals(path));
        };
    }

    static class HybrisMavenImportListener implements MavenImportListener {

        private final Project project;
        private List<VirtualFile> pomList;
        private List<MavenModuleDescriptor> mavenModules;
        private ConfiguratorFactory configuratorFactory;

        public HybrisMavenImportListener(final Project project) {
            this.project = project;
        }

        @Override
        public void importFinished(
            @NotNull final Collection<MavenProject> importedProjects, @NotNull final List<Module> newModules
        ) {
            ApplicationManager.getApplication().invokeLater(() -> {
                if (!project.isDisposed()) {
                    moveMavenModulesToCorrectGroup(
                        getProject(),
                        getMavenModules(),
                        getConfiguratorFactory(),
                        getPomList(),
                        importedProjects,
                        newModules
                    );
                }
            });
        }

        private void setMavenModulesConfig(
            final List<VirtualFile> pomList,
            final List<MavenModuleDescriptor> mavenModules,
            final ConfiguratorFactory configuratorFactory
        ) {
            this.pomList = pomList;
            this.configuratorFactory = configuratorFactory;
            this.mavenModules = Collections.unmodifiableList(mavenModules);
        }


        private Project getProject() {
            return project;
        }

        private List<VirtualFile> getPomList() {
            return Collections.unmodifiableList(pomList);
        }

        private List<MavenModuleDescriptor> getMavenModules() {
            return Collections.unmodifiableList(mavenModules);
        }

        private ConfiguratorFactory getConfiguratorFactory() {
            return configuratorFactory;
        }
    }

    private static void moveMavenModulesToCorrectGroup(
        final @NotNull Project project,
        final @NotNull List<MavenModuleDescriptor> mavenModules,
        final @NotNull ConfiguratorFactory configuratorFactory,
        final List<VirtualFile> pomList,
        final Collection<MavenProject> importedProjects,
        final List<Module> newModules
    ) {
        final List<MavenProject> importedProjectRoots = importedProjects
            .stream()
            .filter(e -> pomList
                .stream()
                .anyMatch(pom -> pom.equals(e.getFile()))
            )
            .toList();

        final List<Module> newRootModules = newModules
            .stream()
            .filter(e -> importedProjectRoots
                .stream()
                .anyMatch(i -> {
                    final String name = i.getName();
                    if (name != null) {
                        return i.getName().equals(e.getName());
                    }
                    return i.getFinalName().startsWith(e.getName());
                })
            )
            .collect(Collectors.toList());

        final Map<String, String[]> mavenGroupMapping = fetchGroupMapping(
            configuratorFactory.getGroupModuleConfigurator(),
            mavenModules
        );
        updateModuleSettings(project, newRootModules, mavenGroupMapping);
    }

    private static void updateModuleSettings(
        final @NotNull Project project,
        final @NotNull List<Module> mavenModules,
        final @NotNull Map<String, String[]> mavenGroupMapping
    ) {
        final ModifiableModuleModel modifiableModuleModel = ReadAction.compute(
            () -> getModifiableModuleModel(project, mavenModules, mavenGroupMapping)
        );
        ApplicationManager.getApplication().invokeAndWait(() -> WriteAction.run(modifiableModuleModel::commit));
    }

    @NotNull
    private static ModifiableModuleModel getModifiableModuleModel(
        final Project project,
        final List<Module> mavenModules,
        final Map<String, String[]> mavenGroupMapping
    ) {
        final var model = ModuleManager.getInstance(project).getModifiableModel();
        final var settingsComponent = HybrisProjectSettingsComponent.getInstance(project);

        mavenModules.stream()
                    .filter(ModuleEx.class::isInstance)
                    .map(ModuleEx.class::cast)
                    .map(settingsComponent::getModuleSettings)
                    .forEach(descriptor -> descriptor.setDescriptorType(HybrisModuleDescriptorType.MAVEN.name()));
        return model;
    }


}
