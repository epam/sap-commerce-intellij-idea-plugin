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
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractModuleDescriptor implements ModuleDescriptor {

    @NotNull
    protected final File moduleRootDirectory;
    @NotNull
    protected final HybrisProjectDescriptor rootProjectDescriptor;
    @NotNull
    private final String name;
    private ModuleDescriptorImportStatus importStatus = ModuleDescriptorImportStatus.UNUSED;

    public AbstractModuleDescriptor(
        @NotNull final File moduleRootDirectory,
        @NotNull final HybrisProjectDescriptor rootProjectDescriptor,
        @NotNull final String name
    ) throws HybrisConfigurationException {
        Validate.notNull(moduleRootDirectory);
        Validate.notNull(rootProjectDescriptor);

        this.moduleRootDirectory = moduleRootDirectory;
        this.rootProjectDescriptor = rootProjectDescriptor;
        this.name = name;

        if (!this.moduleRootDirectory.isDirectory()) {
            throw new HybrisConfigurationException("Can not find module directory using path: " + moduleRootDirectory);
        }
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(@NotNull final ModuleDescriptor o) {
        return this.getName().compareToIgnoreCase(o.getName());
    }

    @NotNull
    @Override
    public File getRootDirectory() {
        return moduleRootDirectory;
    }

    @NotNull
    @Override
    public HybrisProjectDescriptor getRootProjectDescriptor() {
        return rootProjectDescriptor;
    }

    @Override
    public ModuleDescriptorImportStatus getImportStatus() {
        return importStatus;
    }

    @Override
    public void setImportStatus(final ModuleDescriptorImportStatus importStatus) {
        this.importStatus = importStatus;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(this.getName())
            .append(moduleRootDirectory)
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

        final AbstractModuleDescriptor other = (AbstractModuleDescriptor) obj;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
            .append(this.getName(), other.getName())
            .append(moduleRootDirectory, other.moduleRootDirectory)
            .isEquals();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName() + " {");
        sb.append("name='").append(this.getName()).append('\'');
        sb.append(", moduleRootDirectory=").append(moduleRootDirectory);
        sb.append(", moduleFile=").append(YModuleDescriptorUtil.INSTANCE.getIdeaModuleFile(this));
        sb.append('}');
        return sb.toString();
    }
}
