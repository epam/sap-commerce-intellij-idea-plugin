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

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.LibraryDescriptorType;
import com.intellij.idea.plugin.hybris.project.configurators.LibRootsConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCoreExtRegularModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.DefaultJavaLibraryDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.JavaLibraryDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YOotbRegularModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YPlatformModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptorUtil;
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleLibDescriptorUtil;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX;

/**
 * Created 11:45 PM 24 June 2015.
 *
 * @author Vlad Bozhenok <VladBozhenok@gmail.com>
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultLibRootsConfigurator implements LibRootsConfigurator {

    @Override
    public void configure(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull IdeModifiableModelsProvider modifiableModelsProvider,
        @NotNull final ProgressIndicator indicator
    ) {
        final VirtualFile sourceCodeRoot = this.getSourceCodeRoot(moduleDescriptor);

        for (JavaLibraryDescriptor javaLibraryDescriptor : YModuleLibDescriptorUtil.INSTANCE.getLibraryDescriptors(moduleDescriptor)) {
            if (!javaLibraryDescriptor.isValid() && javaLibraryDescriptor.getScope() == DependencyScope.COMPILE) {
                continue;
            }
            if (javaLibraryDescriptor.isDirectoryWithClasses()) {
                this.addClassesToModuleLibs(
                    modifiableRootModel,
                    modifiableModelsProvider,
                    sourceCodeRoot,
                    javaLibraryDescriptor,
                    moduleDescriptor,
                    indicator
                );
            } else {
                this.addJarFolderToModuleLibs(
                    modifiableRootModel,
                    modifiableModelsProvider,
                    sourceCodeRoot,
                    javaLibraryDescriptor,
                    moduleDescriptor,
                    indicator
                );

            }
        }

        if (moduleDescriptor instanceof final YPlatformModuleDescriptor hybrisModuleDescriptor) {
            YModuleLibDescriptorUtil.INSTANCE.createBootstrapLib(hybrisModuleDescriptor, sourceCodeRoot, modifiableModelsProvider);
        }

        if (moduleDescriptor instanceof YCoreExtRegularModuleDescriptor) {
            addLibsToModule(
                modifiableRootModel,
                modifiableModelsProvider,
                HybrisConstants.PLATFORM_LIBRARY_GROUP,
                true
            );
        }

        if (moduleDescriptor instanceof final YOotbRegularModuleDescriptor hybrisModuleDescriptor) {
            if (YModuleDescriptorUtil.INSTANCE.hasBackofficeModule(hybrisModuleDescriptor)) {
                final File backofficeJarDirectory = new File(
                    hybrisModuleDescriptor.getRootDirectory(),
                    HybrisConstants.BACKOFFICE_JAR_DIRECTORY
                );
                if (backofficeJarDirectory.exists()) {
                    YModuleLibDescriptorUtil.INSTANCE.createGlobalLibrary(
                        modifiableModelsProvider,
                        backofficeJarDirectory,
                        HybrisConstants.BACKOFFICE_LIBRARY_GROUP
                    );
                }
            }
            if (moduleDescriptor.getName().equals(HybrisConstants.EXTENSION_NAME_BACK_OFFICE)) {
                addLibsToModule(
                    modifiableRootModel,
                    modifiableModelsProvider,
                    HybrisConstants.BACKOFFICE_LIBRARY_GROUP,
                    true
                );
            }
        }
    }

    @Nullable
    private VirtualFile getSourceCodeRoot(final @NotNull ModuleDescriptor moduleDescriptor) {
        final VirtualFile sourceCodeRoot;
        final File sourceCodeFile = moduleDescriptor.getRootProjectDescriptor().getSourceCodeFile();

        if (null != sourceCodeFile) {
            final VirtualFile sourceVFile = VfsUtil.findFileByIoFile(sourceCodeFile, true);
            if (null == sourceVFile) {
                sourceCodeRoot = null;
            } else if (sourceVFile.isDirectory()) {
                sourceCodeRoot = sourceVFile;
            } else {
                sourceCodeRoot = JarFileSystem.getInstance().getJarRootForLocalFile(sourceVFile);
            }
        } else {
            sourceCodeRoot = null;
        }

        return sourceCodeRoot;
    }

    private void addClassesToModuleLibs(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final IdeModifiableModelsProvider modifiableModelsProvider,
        @Nullable final VirtualFile sourceCodeRoot,
        @NotNull final JavaLibraryDescriptor javaLibraryDescriptor,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ProgressIndicator progressIndicator
    ) {
        final Library library = modifiableRootModel.getModuleLibraryTable().createLibrary();
        final Library.ModifiableModel libraryModifiableModel = modifiableModelsProvider
            .getModifiableLibraryModel(library);
        libraryModifiableModel.addRoot(
            VfsUtil.getUrlForLibraryRoot(javaLibraryDescriptor.getLibraryFile()), OrderRootType.CLASSES
        );

        boolean sourceDirAttached = false;
        if (null != javaLibraryDescriptor.getSourcesFile()) {
            final VirtualFile srcDirVF = VfsUtil.findFileByIoFile(javaLibraryDescriptor.getSourcesFile(), true);
            if (null != srcDirVF) {
                libraryModifiableModel.addRoot(srcDirVF, OrderRootType.SOURCES);
                sourceDirAttached = true;
            }
        }

        if (sourceCodeRoot != null && !sourceDirAttached && javaLibraryDescriptor.getLibraryFile().getName().endsWith(
            HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX)) {
            libraryModifiableModel.addRoot(sourceCodeRoot, OrderRootType.SOURCES);
        }


        if (javaLibraryDescriptor.isExported()) {
            this.setLibraryEntryExported(modifiableRootModel, library);
        }
        setLibraryEntryScope(modifiableRootModel, library, javaLibraryDescriptor.getScope());
    }

    private void addJarFolderToModuleLibs(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final IdeModifiableModelsProvider modifiableModelsProvider,
        @Nullable final VirtualFile sourceCodeRoot,
        @NotNull final JavaLibraryDescriptor javaLibraryDescriptor,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ProgressIndicator progressIndicator
    ) {
        final LibraryTable projectLibraryTable = modifiableRootModel.getModuleLibraryTable();

        final Library library = projectLibraryTable.createLibrary();
        final Library.ModifiableModel libraryModifiableModel = modifiableModelsProvider
            .getModifiableLibraryModel(library);

        libraryModifiableModel.addJarDirectory(
            VfsUtil.getUrlForLibraryRoot(javaLibraryDescriptor.getLibraryFile()), true
        );

        if (null != javaLibraryDescriptor.getSourcesFile()) {
            final VirtualFile srcDirVF = VfsUtil.findFileByIoFile(javaLibraryDescriptor.getSourcesFile(), true);
            if (null != srcDirVF) {
                libraryModifiableModel.addRoot(srcDirVF, OrderRootType.SOURCES);
            }
        }

        if (javaLibraryDescriptor.isExported()) {
            this.setLibraryEntryExported(modifiableRootModel, library);
        }
        setLibraryEntryScope(modifiableRootModel, library, javaLibraryDescriptor.getScope());

        final List<String> mavenSources = resolveMavenSources(
            modifiableRootModel,
            javaLibraryDescriptor,
            moduleDescriptor,
            progressIndicator
        );

        final List<String> standardSources = resolveStandardProvidedSources(
            javaLibraryDescriptor,
            moduleDescriptor
        );

        final List<String> resultLibs = new ArrayList<>(mavenSources);
        resultLibs.addAll(standardSources);

        for (String resultLib : resultLibs) {
            libraryModifiableModel.addRoot("jar://" + resultLib + "!/", OrderRootType.SOURCES);
        }
    }

    public static List<String> resolveMavenSources(
        final @NotNull ModifiableRootModel modifiableRootModel,
        final @NotNull JavaLibraryDescriptor javaLibraryDescriptor,
        final @NotNull ModuleDescriptor moduleDescriptor,
        final @NotNull ProgressIndicator progressIndicator
    ) {
        if (javaLibraryDescriptor instanceof DefaultJavaLibraryDescriptor) {
            final var defaultJavaLibraryDescriptor = (DefaultJavaLibraryDescriptor) javaLibraryDescriptor;
            if (LibraryDescriptorType.LIB == defaultJavaLibraryDescriptor.getDescriptorType()) {
                return MavenUtils.resolveMavenSources(modifiableRootModel, moduleDescriptor, progressIndicator);
            }
        }
        return Collections.emptyList();
    }

    public static List<String> resolveStandardProvidedSources(
        final @NotNull JavaLibraryDescriptor javaLibraryDescriptor,
        final @NotNull ModuleDescriptor moduleDescriptor
    ) {
        final HybrisApplicationSettings appSettings = HybrisApplicationSettingsComponent.getInstance().getState();
        if (!appSettings.getWithStandardProvidedSources()) {
            return Collections.emptyList();
        }

        if (javaLibraryDescriptor instanceof DefaultJavaLibraryDescriptor) {
            final var defaultJavaLibraryDescriptor = (DefaultJavaLibraryDescriptor) javaLibraryDescriptor;
            if (LibraryDescriptorType.WEB_INF_LIB == defaultJavaLibraryDescriptor.getDescriptorType()) {
                var sourcesDirectory = new File(
                    moduleDescriptor.getRootDirectory(),
                    HybrisConstants.DOC_SOURCES_JAR_DIRECTORY
                );

                final String[] filesArray = sourcesDirectory.list((file, name) -> name.endsWith("-sources.jar"));
                if (filesArray == null) {
                    return Collections.emptyList();
                }
                return Arrays.stream(filesArray)
                             .map(fileName -> new File(sourcesDirectory,fileName).getAbsolutePath())
                             .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private void addLibsToModule(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull IdeModifiableModelsProvider modifiableModelsProvider,
        @NotNull final String libraryName,
        final boolean export
    ) {
        Validate.notNull(modifiableRootModel);

        final LibraryTable.ModifiableModel libraryTableModifiableModel = modifiableModelsProvider
            .getModifiableProjectLibrariesModel();

        Library library = libraryTableModifiableModel.getLibraryByName(libraryName);

        if (null == library) {
            library = libraryTableModifiableModel.createLibrary(libraryName);
        }
        modifiableRootModel.addLibraryEntry(library);

        if (export) {
            setLibraryEntryExported(modifiableRootModel, library);
        }
    }

    private void setLibraryEntryExported(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final Library library
    ) {
        final LibraryOrderEntry libraryOrderEntry = findOrderEntryForLibrary(modifiableRootModel, library);

        if (libraryOrderEntry != null) {
            libraryOrderEntry.setExported(true);
        }
    }

    private void setLibraryEntryScope(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final Library library,
        @NotNull DependencyScope scope
    ) {
        final LibraryOrderEntry entry = findOrderEntryForLibrary(modifiableRootModel, library);

        if (entry != null) {
            entry.setScope(scope);
        }
    }

    private static LibraryOrderEntry findOrderEntryForLibrary(
        final @NotNull ModifiableRootModel modifiableRootModel,
        final @NotNull Library library
    ) {
        // Workaround of using Library.equals in findLibraryOrderEntry, which doesn't work here, because all empty libs are equal. Use == instead.
        return (LibraryOrderEntry) Arrays
            .stream(modifiableRootModel.getOrderEntries())
            .filter(entry -> entry instanceof LibraryOrderEntry && ((LibraryOrderEntry) entry).getLibrary() == library)
            .findFirst()
            .orElse(modifiableRootModel.findLibraryOrderEntry(library));
    }
}
