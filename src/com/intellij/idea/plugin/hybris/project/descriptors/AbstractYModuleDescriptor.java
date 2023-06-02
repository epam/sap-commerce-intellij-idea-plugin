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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractYModuleDescriptor extends AbstractModuleDescriptor implements YModuleDescriptor {

    @NotNull
    protected final Set<YModuleDescriptor> dependenciesTree = new LinkedHashSet<>(0);
    @NotNull
    protected Set<String> springFileSet = new LinkedHashSet<>();

    public AbstractYModuleDescriptor(
        final @NotNull File moduleRootDirectory,
        final @NotNull HybrisProjectDescriptor rootProjectDescriptor,
        final @NotNull String name
    ) throws HybrisConfigurationException {
        super(moduleRootDirectory, rootProjectDescriptor, name);
    }

    @NotNull
    @Override
    public Set<YModuleDescriptor> getDependenciesTree() {
        return dependenciesTree;
    }

    @Override
    public void setDependenciesTree(@NotNull final Set<YModuleDescriptor> moduleDescriptors) {
        Validate.notNull(moduleDescriptors);

        this.dependenciesTree.clear();
        this.dependenciesTree.addAll(moduleDescriptors);
    }

    @NotNull
    @Override
    public Set<String> getSpringFileSet() {
        return springFileSet;
    }

    @Override
    public boolean addSpringFile(@NotNull final String springFile) {
        return this.springFileSet.add(springFile);
    }
}
