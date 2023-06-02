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

import com.intellij.idea.plugin.hybris.common.LibraryDescriptorType;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.util.io.FileUtil;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class DefaultJavaLibraryDescriptor implements JavaLibraryDescriptor {

    @NotNull
    private final File libraryFile;
    private final File sourcesFile;
    private final boolean isExported;
    private final boolean isDirectoryWithClasses;
    private final LibraryDescriptorType descriptorType;

    @NotNull
    private final DependencyScope scope;

    public DefaultJavaLibraryDescriptor(
        @NotNull final File libraryFile,
        final boolean isExported
    ) {
        Validate.notNull(libraryFile);

        this.libraryFile = libraryFile;
        this.sourcesFile = null;
        this.isExported = isExported;
        this.isDirectoryWithClasses = false;
        this.scope = DependencyScope.COMPILE;
        this.descriptorType = LibraryDescriptorType.UNKNOWN;
    }

    public DefaultJavaLibraryDescriptor(
        @NotNull final File libraryFile,
        final boolean isExported,
        final LibraryDescriptorType descriptorType
    ) {
        Validate.notNull(libraryFile);

        this.libraryFile = libraryFile;
        this.sourcesFile = null;
        this.isExported = isExported;
        this.isDirectoryWithClasses = false;
        this.scope = DependencyScope.COMPILE;
        this.descriptorType = descriptorType;
    }

    public DefaultJavaLibraryDescriptor(
        @NotNull final File libraryFile,
        final boolean isExported,
        final boolean isDirectoryWithClasses
    ) {
        Validate.notNull(libraryFile);

        this.libraryFile = libraryFile;
        this.sourcesFile = null;
        this.isExported = isExported;
        this.isDirectoryWithClasses = isDirectoryWithClasses;
        this.scope = DependencyScope.COMPILE;
        descriptorType = LibraryDescriptorType.UNKNOWN;
    }

    public DefaultJavaLibraryDescriptor(
        @NotNull final File libraryFile,
        @Nullable final File sourcesFile,
        final boolean isExported,
        final boolean isDirectoryWithClasses
    ) {
        Validate.notNull(libraryFile);

        this.libraryFile = libraryFile;
        this.sourcesFile = sourcesFile;
        this.isExported = isExported;
        this.isDirectoryWithClasses = isDirectoryWithClasses;
        this.scope = DependencyScope.COMPILE;
        descriptorType = LibraryDescriptorType.UNKNOWN;
    }

    @NotNull
    @Override
    public File getLibraryFile() {
        return libraryFile;
    }

    @Nullable
    @Override
    public File getSourcesFile() {
        return sourcesFile;
    }

    @Override
    public boolean isExported() {
        return isExported;
    }

    @Override
    public boolean isDirectoryWithClasses() {
        return isDirectoryWithClasses;
    }

    @Override
    @NotNull
    public DependencyScope getScope() {
        return scope;
    }

    @Override
    public boolean isValid() {
        return libraryFile.exists();
    }

    @Override
    public int compareTo(@NotNull final JavaLibraryDescriptor o) {
        return FileUtil.compareFiles(this.libraryFile, o.getLibraryFile());
    }

    public LibraryDescriptorType getDescriptorType() {
        return descriptorType;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(libraryFile)
            .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }

        final DefaultJavaLibraryDescriptor other = (DefaultJavaLibraryDescriptor) obj;

        return new EqualsBuilder()
            .append(libraryFile, other.libraryFile)
            .isEquals();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultJavaLibraryDescriptor{");
        sb.append("libraryFile=").append(libraryFile);
        sb.append(", sourcesFile=").append(sourcesFile);
        sb.append(", isExported=").append(isExported);
        sb.append(", isDirectoryWithClasses=").append(isDirectoryWithClasses);
        sb.append('}');
        return sb.toString();
    }
}
