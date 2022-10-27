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

package com.intellij.idea.plugin.hybris.project.descriptors;

import com.google.common.collect.Sets;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.ExistingLibraryEditor;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created 3:55 PM 13 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class PlatformHybrisModuleDescriptor extends AbstractHybrisModuleDescriptor {

    public PlatformHybrisModuleDescriptor(
        @NotNull final File moduleRootDirectory,
        @NotNull final HybrisProjectDescriptor rootProjectDescriptor
    ) throws HybrisConfigurationException {
        super(moduleRootDirectory, rootProjectDescriptor);
    }

    @NotNull
    @Override
    public String getName() {
        return HybrisConstants.EXTENSION_NAME_PLATFORM;
    }

    @NotNull
    @Override
    public Set<String> getRequiredExtensionNames() {
        final File extDirectory = new File(this.getRootDirectory(), HybrisConstants.PLATFORM_EXTENSIONS_DIRECTORY_NAME);

        final Set<String> platformDependencies = Sets.newHashSet();

        if (extDirectory.isDirectory()) {
            final File[] files = extDirectory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);

            if (null != files) {
                for (File file : files) {
                    platformDependencies.add(file.getName());
                }
            }
        }

        return Collections.unmodifiableSet(platformDependencies);
    }

    @NotNull
    @Override
    public List<JavaLibraryDescriptor> getLibraryDescriptors() {
        return Collections.singletonList(
            new DefaultJavaLibraryDescriptor(
                getDbDriversDirectory(), true, false
            )
        );
    }

    private File getDbDriversDirectory() {
        final File dbDriversDirectory = getRootProjectDescriptor().getExternalDbDriversDirectory();
        if (dbDriversDirectory != null) {
            return dbDriversDirectory;
        }
        return new File(this.getRootDirectory(), HybrisConstants.PLATFORM_DB_DRIVER);
    }

    @Override
    public boolean isPreselected() {
        return true;
    }

    public void createBootstrapLib(
        @Nullable final VirtualFile sourceCodeRoot,
        @NotNull final IdeModifiableModelsProvider modifiableModelsProvider
    ) {

        final Collection<File> libraryDirectories = getLibraryDirectories();
        final File bootStrapSrc = new File(getRootDirectory(), HybrisConstants.PL_BOOTSTRAP_GEN_SRC_DIRECTORY);

        final LibraryTable.ModifiableModel libraryTableModifiableModel = modifiableModelsProvider
            .getModifiableProjectLibrariesModel();

        Library library = libraryTableModifiableModel.getLibraryByName(HybrisConstants.PLATFORM_LIBRARY_GROUP);
        if (null == library) {
            library = libraryTableModifiableModel.createLibrary(HybrisConstants.PLATFORM_LIBRARY_GROUP);
        }

        if (libraryTableModifiableModel instanceof LibrariesModifiableModel) {
            final ExistingLibraryEditor existingLibraryEditor = ((LibrariesModifiableModel) libraryTableModifiableModel)
                .getLibraryEditor(library);

            for (final File libRoot : libraryDirectories) {
                existingLibraryEditor.addJarDirectory(
                    VfsUtil.getUrlForLibraryRoot(libRoot), true, OrderRootType.CLASSES
                );

                if (null != sourceCodeRoot) {
                    if (sourceCodeRoot.getFileSystem() instanceof JarFileSystem) {
                        existingLibraryEditor.addJarDirectory(sourceCodeRoot, true, OrderRootType.SOURCES);
                    } else {
                        existingLibraryEditor.addRoot(sourceCodeRoot, OrderRootType.SOURCES);
                    }
                }
            }

            existingLibraryEditor.addRoot(
                VfsUtil.getUrlForLibraryRoot(bootStrapSrc), OrderRootType.SOURCES
            );

        } else {
            final Library.ModifiableModel model = modifiableModelsProvider.getModifiableLibraryModel(library);

            for (final File libRoot : libraryDirectories) {
                model.addJarDirectory(VfsUtil.getUrlForLibraryRoot(libRoot), true);
            }
            model.addRoot(VfsUtil.getUrlForLibraryRoot(bootStrapSrc), OrderRootType.SOURCES);
        }

    }

    private Collection<File> getLibraryDirectories() {
        final Collection<File> libraryDirectories = new ArrayList<>();
        final File resourcesDirectory = new File(this.getRootDirectory(), HybrisConstants.RESOURCES_DIRECTORY);

        if (resourcesDirectory.exists()) {
            final File[] resourcesInnerDirectories = resourcesDirectory.listFiles(
                (FileFilter) DirectoryFileFilter.DIRECTORY
            );

            if (null != resourcesInnerDirectories) {

                for (File resourcesInnerDirectory : resourcesInnerDirectories) {
                    File file = new File(resourcesInnerDirectory, HybrisConstants.LIB_DIRECTORY);

                    if (file.exists()) {
                        libraryDirectories.add(file);
                    }

                    file = new File(resourcesInnerDirectory, HybrisConstants.BIN_DIRECTORY);
                    if (file.exists()) {
                        libraryDirectories.add(file);
                    }
                }
            }
        }

        addLibraryDirectories(libraryDirectories, new File(getRootDirectory(), HybrisConstants.PL_BOOTSTRAP_LIB_DIRECTORY));

        addLibraryDirectories(libraryDirectories, new File(getRootDirectory(), HybrisConstants.PL_TOMCAT_BIN_DIRECTORY));
        addLibraryDirectories(libraryDirectories, new File(getRootDirectory(), HybrisConstants.PL_TOMCAT_6_BIN_DIRECTORY));
        addLibraryDirectories(libraryDirectories, new File(getRootDirectory(), HybrisConstants.PL_TOMCAT_LIB_DIRECTORY));
        addLibraryDirectories(libraryDirectories, new File(getRootDirectory(), HybrisConstants.PL_TOMCAT_6_LIB_DIRECTORY));

        return libraryDirectories;
    }

    private void addLibraryDirectories(final Collection<File> libraryDirectories, final File file) {
        if (file.exists()) {
            libraryDirectories.add(file);
        }
    }

    @Override
    public HybrisModuleDescriptorType getDescriptorType() {
        return HybrisModuleDescriptorType.PLATFORM;
    }
}
