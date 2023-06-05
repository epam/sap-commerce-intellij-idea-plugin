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

import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YBackofficeSubModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YWebSubModuleDescriptor;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.vfs.VfsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaResourceRootType;

import java.io.File;
import java.util.List;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.*;

public class ReadOnlyContentRootConfigurator extends RegularContentRootConfigurator {

    @Override
    protected void configureCommonRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore
    ) {
        configureResourceDirectory(contentEntry, moduleDescriptor, dirsToIgnore);

        excludeCommonNeedlessDirs(contentEntry, moduleDescriptor);
    }

    @Override
    protected void configureAdditionalRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final String directoryName,
        @NotNull final ContentEntry contentEntry,
        @NotNull final File parentDirectory
    ) {
        final File additionalModuleDirectory = new File(parentDirectory, directoryName);
        if (!additionalModuleDirectory.exists() || additionalModuleDirectory.isFile()) {
            return;
        }

        final File additionalClassesDirectory = new File(additionalModuleDirectory, CLASSES_DIRECTORY);
        contentEntry.addExcludeFolder(VfsUtil.pathToUrl(additionalClassesDirectory.getAbsolutePath()));

        final File additionalResourcesDirectory = new File(additionalModuleDirectory, RESOURCES_DIRECTORY);
        contentEntry.addSourceFolder(
            VfsUtil.pathToUrl(additionalResourcesDirectory.getAbsolutePath()),
            JavaResourceRootType.RESOURCE
        );
    }

    @Override
    protected void configureRoots(
        @NotNull final YBackofficeSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore
    ) {
        final File classesDirectory = new File(moduleDescriptor.getModuleRootDirectory(), CLASSES_DIRECTORY);
        contentEntry.addExcludeFolder(VfsUtil.pathToUrl(classesDirectory.getAbsolutePath()));

        final File resourcesDirectory = new File(moduleDescriptor.getModuleRootDirectory(), RESOURCES_DIRECTORY);
        contentEntry.addSourceFolder(
            VfsUtil.pathToUrl(resourcesDirectory.getAbsolutePath()),
            JavaResourceRootType.RESOURCE
        );
    }

    @Override
    protected void configureRoots(
        @NotNull final YWebSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore
    ) {
        final File webAddonSrcDirectory = new File(moduleDescriptor.getModuleRootDirectory(), ADDON_SRC_DIRECTORY);
        contentEntry.addExcludeFolder(VfsUtil.pathToUrl(webAddonSrcDirectory.getAbsolutePath()));

        final File webTestClassesDirectory = new File(moduleDescriptor.getModuleRootDirectory(), TEST_CLASSES_DIRECTORY);
        contentEntry.addExcludeFolder(VfsUtil.pathToUrl(webTestClassesDirectory.getAbsolutePath()));

        final File commonWebSrcDirectory = new File(moduleDescriptor.getModuleRootDirectory(), COMMON_WEB_SRC_DIRECTORY);
        contentEntry.addExcludeFolder(VfsUtil.pathToUrl(commonWebSrcDirectory.getAbsolutePath()));
    }
}
