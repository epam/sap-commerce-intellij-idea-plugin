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

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType.CONFIG;
import static com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType.CUSTOM;

/**
 * Created 3:55 PM 13 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class ConfigHybrisModuleDescriptor extends AbstractHybrisModuleDescriptor {

    private boolean preselected;
    private boolean mainConfig;

    public ConfigHybrisModuleDescriptor(
        @NotNull final File moduleRootDirectory,
        @NotNull final HybrisProjectDescriptor rootProjectDescriptor,
        @NotNull final String name
    ) throws HybrisConfigurationException {
        super(moduleRootDirectory, rootProjectDescriptor, name);
    }

    @NotNull
    @Override
    public Set<String> getRequiredExtensionNames() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public List<JavaLibraryDescriptor> getLibraryDescriptors() {
        return Collections.<JavaLibraryDescriptor>singletonList(new DefaultJavaLibraryDescriptor(
            new File(this.getRootDirectory(), HybrisConstants.CONFIG_LICENCE_DIRECTORY), true
        ));
    }

    @Override
    public boolean isPreselected() {
        return preselected;
    }

    public void setPreselected(final boolean preselected) {
        this.preselected = preselected;
    }

    @Override
    public HybrisModuleDescriptorType getDescriptorType() {
        return isMainConfig() ? CONFIG : CUSTOM;
    }

    public void setMainConfig(final boolean mainConfig) {
        this.mainConfig = mainConfig;
    }

    public boolean isMainConfig() {
        return mainConfig;
    }
}
