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

import com.intellij.idea.plugin.hybris.settings.ExtensionDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface HybrisModuleDescriptor extends Comparable<HybrisModuleDescriptor> {

    enum IMPORT_STATUS {MANDATORY, UNUSED}

    @NotNull
    String getName();

    @NotNull
    File getRootDirectory();

    @NotNull
    default String getRelativePath() {
        return YModuleDescriptorUtil.INSTANCE.getRelativePath(this);
    }

    @NotNull
    HybrisProjectDescriptor getRootProjectDescriptor();

    @NotNull
    default File getIdeaModuleFile() {
        return YModuleDescriptorUtil.INSTANCE.getIdeaModuleFile(this);
    }

    @NotNull
    default Set<String> getRequiredExtensionNames() {
        return YModuleDescriptorUtil.INSTANCE.getRequiredExtensionNames(this);
    }

    @NotNull
    Set<HybrisModuleDescriptor> getDependenciesTree();

    void setDependenciesTree(@NotNull Set<HybrisModuleDescriptor> moduleDescriptors);

    @NotNull
    default Set<HybrisModuleDescriptor> getDependenciesPlainList() {
        return YModuleDescriptorUtil.INSTANCE.getDependenciesPlainList(this);
    }

    default List<JavaLibraryDescriptor> getLibraryDescriptors() {
        return YModuleLibDescriptorUtil.INSTANCE.getLibraryDescriptors(this);
    }

    default boolean isPreselected() {
        return YModuleDescriptorUtil.INSTANCE.isPreselected(this);
    }

    boolean isInLocalExtensions();

    void setInLocalExtensions(boolean inLocalExtensions);

    @NotNull
    Set<String> getSpringFileSet();

    boolean addSpringFile(@NotNull String springFile);

    @Nullable
    default File getWebRoot() {
        return YModuleDescriptorUtil.INSTANCE.getWebRoot(this);
    }

    /**
     * This method will return true if module has `kotlinsrc` or `kotlintestsrc` directories
     */
    default boolean hasKotlinSourceDirectories() {
        return YModuleDescriptorUtil.INSTANCE.hasKotlinDirectories(this);
    }

    default HybrisModuleDescriptorType getDescriptorType() {
        return YModuleDescriptorUtil.INSTANCE.getDescriptorType(this);
    }

    void setImportStatus(IMPORT_STATUS importStatus);

    IMPORT_STATUS getImportStatus();

    @NotNull default ExtensionDescriptor getExtensionDescriptor() {
        return YModuleDescriptorUtil.INSTANCE.getExtensionDescriptor(this);
    }
}
