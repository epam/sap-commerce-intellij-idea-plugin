/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenArtifactDownloader;
import org.jetbrains.idea.maven.project.MavenEmbeddersManager;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProjectReader;
import org.jetbrains.idea.maven.project.MavenProjectReaderProjectLocator;
import org.jetbrains.idea.maven.project.MavenProjectReaderResult;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.project.MavenWorkspaceSettings;
import org.jetbrains.idea.maven.project.MavenWorkspaceSettingsComponent;
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface MavenUtils {

    Logger LOG = LoggerFactory.getLogger(MavenUtils.class);

    @NotNull
     static List<String> resolveMavenJavadocs(
        final @NotNull ModifiableRootModel modifiableRootModel,
        final @NotNull ModuleDescriptor moduleDescriptor,
        final @NotNull ProgressIndicator progressIndicator
    ) {
        final HybrisApplicationSettings appSettings = HybrisApplicationSettingsComponent.getInstance().getState();
        if (appSettings.getWithMavenJavadocs()) {
            return resolveMavenDependencies(modifiableRootModel, moduleDescriptor, progressIndicator, false, true);
        }
        return Collections.emptyList();
    }

     static List<String> resolveMavenSources(
        final @NotNull ModifiableRootModel modifiableRootModel,
        final @NotNull ModuleDescriptor moduleDescriptor,
        final @NotNull ProgressIndicator progressIndicator
    ) {
        final HybrisApplicationSettings appSettings = HybrisApplicationSettingsComponent.getInstance().getState();
        if (appSettings.getWithMavenSources()) {
            return resolveMavenDependencies(modifiableRootModel, moduleDescriptor, progressIndicator, true, false);
        }
        return Collections.emptyList();
    }

    @NotNull
    private static List<String> resolveMavenDependencies(
        final @NotNull ModifiableRootModel modifiableRootModel,
        final @NotNull ModuleDescriptor moduleDescriptor,
        final @NotNull ProgressIndicator progressIndicator, final boolean downloadSources, final boolean downloadDocs
    ) {
        final List<String> resultPathList = new ArrayList<>();

        final File moduleDir = moduleDescriptor.getRootDirectory();
        final File mavenDescriptorFile = new File(moduleDir, "external-dependencies.xml");
        if (mavenDescriptorFile.exists()) {
            final MavenProjectReader mavenProjectReader = new MavenProjectReader(modifiableRootModel.getProject());
            final VirtualFile vfsMavenDescriptor = VfsUtil.findFileByIoFile(mavenDescriptorFile, false);

            final @NotNull Project project = modifiableRootModel.getProject();
            final MavenWorkspaceSettings settings = MavenWorkspaceSettingsComponent.getInstance(project).getSettings();

            final String moduleDirPath = moduleDir.getAbsolutePath();
            final MavenEmbeddersManager embeddersManager = new MavenEmbeddersManager(project);
            final MavenEmbedderWrapper embedder = embeddersManager.getEmbedder(
                MavenEmbeddersManager.FOR_DEPENDENCIES_RESOLVE,
                moduleDirPath,
                moduleDirPath
            );
            final MavenGeneralSettings generalSettings = settings.getGeneralSettings();
            generalSettings.setNonRecursive(true);

            final MavenProjectsTree mavenProjectsTree = new MavenProjectsTree(project);
            final MavenProjectReaderProjectLocator myProjectLocator = mavenProjectsTree.getProjectLocator();

            try {
                final Collection<MavenProjectReaderResult> mavenProjects = mavenProjectReader.resolveProject(
                    generalSettings,
                    embedder,
                    Collections.singleton(vfsMavenDescriptor),
                    MavenExplicitProfiles.NONE,
                    myProjectLocator
                );

                for (MavenProjectReaderResult mavenProjectReaderResult : mavenProjects) {
                    final MavenProjectsManager manager = MavenProjectsManager.getInstance(project);

                    final MavenProgressIndicator indicator = new MavenProgressIndicator(project, manager::getSyncConsole);
                    indicator.setIndicator(progressIndicator);
                    final List<MavenArtifact> dependencies = mavenProjectReaderResult.mavenModel.getDependencies();

                    mavenProjectsTree.resetManagedFilesAndProfiles(
                        Collections.singletonList(vfsMavenDescriptor),
                        MavenExplicitProfiles.NONE
                    );
                    mavenProjectsTree.updateAll(false, generalSettings, indicator);

                    mavenProjectsTree.getProjects().get(0).getDependencies().addAll(dependencies);
                    final MavenArtifactDownloader.DownloadResult downloadResult = MavenArtifactDownloader.download(
                        project,
                        mavenProjectsTree,
                        mavenProjectsTree.getProjects(),
                        dependencies,
                        downloadSources,
                        downloadDocs,
                        embedder,
                        indicator
                    );
                    if (downloadDocs) {
                        for (final MavenId resolvedDoc : downloadResult.resolvedDocs) {
                            final Path libFile = getArtifactLib(manager, resolvedDoc);
                            final String resultJarPath = libFile.toAbsolutePath().toString().replace(".jar", "-javadoc.jar");
                            resultPathList.add(resultJarPath);
                        }
                    }
                    if (downloadSources) {
                        for (final MavenId resolvedDoc : downloadResult.resolvedSources) {
                            final Path libFile = getArtifactLib(manager, resolvedDoc);
                            final String resultJarPath = libFile.toAbsolutePath().toString().replace(".jar", "-sources.jar");
                            resultPathList.add(resultJarPath);
                        }
                    }
                }

            } catch (MavenProcessCanceledException e) {
                LOG.error("Unable to generate pseudo-maven dependencies", e);
            }
            resultPathList.sort(String::compareTo);
        }
        return resultPathList;
    }

    @NotNull
    static Path getArtifactLib(final MavenProjectsManager manager, final MavenId resolvedDoc) {
        return MavenArtifactUtil
            .getArtifactNioPath(
                manager.getLocalRepository(),
                resolvedDoc.getGroupId(),
                resolvedDoc.getArtifactId(),
                resolvedDoc.getVersion(),
                "jar"
            );
    }
}
