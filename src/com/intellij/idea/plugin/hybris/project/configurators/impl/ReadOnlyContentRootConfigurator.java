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
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.vfs.VfsUtil;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaResourceRootType;

import java.io.File;
import java.util.List;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.ADDON_SRC_DIRECTORY;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.BACKOFFICE_MODULE_DIRECTORY;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.CLASSES_DIRECTORY;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.COMMON_WEB_SRC_DIRECTORY;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.RESOURCES_DIRECTORY;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.TEST_CLASSES_DIRECTORY;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.WEB_MODULE_DIRECTORY;

/**
 * Created by Martin Zdarsky-Jones <zdary@zdary.cz> on 12/4/17.
 */
public class ReadOnlyContentRootConfigurator extends RegularContentRootConfigurator {

    protected void configureCommonRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore
    ) {
        configureResourceDirectory(contentEntry, moduleDescriptor, dirsToIgnore);
        
        excludeCommonNeedlessDirs(contentEntry, moduleDescriptor);
    }

    protected void configureAdditionalRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final String directoryName,
        @NotNull final ContentEntry contentEntry,
        @NotNull final File parentDirectory
    ) {
        Validate.notNull(moduleDescriptor);
        Validate.notNull(directoryName);
        Validate.notNull(contentEntry);
        Validate.notNull(parentDirectory);

        final File additionalModuleDirectory = new File(parentDirectory, directoryName);
        if (!additionalModuleDirectory.exists() || additionalModuleDirectory.isFile()) {
            return;
        }

        final File additionalClassesDirectory = new File(additionalModuleDirectory, CLASSES_DIRECTORY);
        contentEntry.addExcludeFolder(
            VfsUtil.pathToUrl(additionalClassesDirectory.getAbsolutePath())
        );

        final File additionalResourcesDirectory = new File(additionalModuleDirectory, RESOURCES_DIRECTORY);
        contentEntry.addSourceFolder(
            VfsUtil.pathToUrl(additionalResourcesDirectory.getAbsolutePath()),
            JavaResourceRootType.RESOURCE
        );
    }

    protected void configureBackOfficeRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore
    ) {
        Validate.notNull(moduleDescriptor);
        Validate.notNull(contentEntry);

        final File backOfficeModuleDirectory = new File(
            moduleDescriptor.getRootDirectory(), BACKOFFICE_MODULE_DIRECTORY
        );

        final File hmcClassesDirectory = new File(backOfficeModuleDirectory, CLASSES_DIRECTORY);
        contentEntry.addExcludeFolder(
            VfsUtil.pathToUrl(hmcClassesDirectory.getAbsolutePath())
        );

        final File hmcResourcesDirectory = new File(backOfficeModuleDirectory, RESOURCES_DIRECTORY);
        contentEntry.addSourceFolder(
            VfsUtil.pathToUrl(hmcResourcesDirectory.getAbsolutePath()),
            JavaResourceRootType.RESOURCE
        );
    }

    @Override
    protected void configureRegularWebRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore
    ) {
        final File webModuleDirectory = new File(moduleDescriptor.getRootDirectory(), WEB_MODULE_DIRECTORY);

        final File webAddonSrcDirectory = new File(webModuleDirectory, ADDON_SRC_DIRECTORY);
        contentEntry.addExcludeFolder(
            VfsUtil.pathToUrl(webAddonSrcDirectory.getAbsolutePath())
        );

        final File webTestClassesDirectory = new File(webModuleDirectory, TEST_CLASSES_DIRECTORY);
        contentEntry.addExcludeFolder(
            VfsUtil.pathToUrl(webTestClassesDirectory.getAbsolutePath())
        );

        final File commonWebSrcDirectory = new File(webModuleDirectory, COMMON_WEB_SRC_DIRECTORY);
        contentEntry.addExcludeFolder(
            VfsUtil.pathToUrl(commonWebSrcDirectory.getAbsolutePath())
        );
    }
}
