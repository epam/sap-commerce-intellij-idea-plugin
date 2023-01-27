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

import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ExtensionInfo;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.ExistingLibraryEditor;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import com.intellij.openapi.vfs.VfsUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Martin Zdarsky-Jones on 19/08/2016.
 */
public class OotbHybrisModuleDescriptor extends RegularHybrisModuleDescriptor {

    public OotbHybrisModuleDescriptor(
        @NotNull final File moduleRootDirectory,
        @NotNull final HybrisProjectDescriptor rootProjectDescriptor,
        @NotNull final ExtensionInfo extensionInfo
    ) throws HybrisConfigurationException {
        super(moduleRootDirectory, rootProjectDescriptor, extensionInfo);
    }

    @Override
    public HybrisModuleDescriptorType getDescriptorType() {
        return HybrisModuleDescriptorType.OOTB;
    }

    public void createGlobalLibrary(
        @NotNull final IdeModifiableModelsProvider modifiableModelsProvider,
        @NotNull final File libraryDirRoot,
        @NotNull final String libraryName
    ) {
        final LibraryTable.ModifiableModel libraryTableModifiableModel = modifiableModelsProvider
            .getModifiableProjectLibrariesModel();

        Library library = libraryTableModifiableModel.getLibraryByName(libraryName);
        if (null == library) {
            library = libraryTableModifiableModel.createLibrary(libraryName);
        }

        if (libraryTableModifiableModel instanceof LibrariesModifiableModel) {
            final ExistingLibraryEditor existingLibraryEditor = ((LibrariesModifiableModel) libraryTableModifiableModel)
                .getLibraryEditor(library);
            existingLibraryEditor.addJarDirectory(
                VfsUtil.getUrlForLibraryRoot(libraryDirRoot), true, OrderRootType.CLASSES
            );
        } else {
            final Library.ModifiableModel libraryModifiableModel = modifiableModelsProvider
                .getModifiableLibraryModel(library);
            libraryModifiableModel.addJarDirectory(VfsUtil.getUrlForLibraryRoot(libraryDirRoot), true);
        }
    }
}
