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

package com.intellij.idea.plugin.hybris.project;

import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.projectImport.ProjectImportBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * Created 5:20 PM 13 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public abstract class AbstractHybrisProjectImportBuilder extends ProjectImportBuilder<ModuleDescriptor> {

    public abstract void setRootProjectDirectory(@NotNull final File directory);

    @NotNull
    public abstract HybrisProjectDescriptor getHybrisProjectDescriptor();

    public abstract void setAllModuleList();

    public abstract List<ModuleDescriptor> getBestMatchingExtensionsToImport(@Nullable HybrisProjectSettings settings);

    public abstract void setCoreStepModuleList();

    public abstract void setExternalStepModuleList();

    public abstract void setHybrisModulesToImport(final List<ModuleDescriptor> hybrisModules);

    public abstract List<ModuleDescriptor> getHybrisModulesToImport();

    public abstract void setRefresh(boolean refresh);
}
