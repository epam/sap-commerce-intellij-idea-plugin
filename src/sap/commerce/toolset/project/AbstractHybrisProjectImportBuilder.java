/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project;

import com.intellij.projectImport.ProjectImportBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor;
import sap.commerce.toolset.project.descriptor.ModuleDescriptor;
import sap.commerce.toolset.project.settings.ProjectSettings;

import java.io.File;
import java.util.List;

public abstract class AbstractHybrisProjectImportBuilder extends ProjectImportBuilder<ModuleDescriptor> {

    public abstract void setRootProjectDirectory(@NotNull final File directory);

    @NotNull
    public abstract HybrisProjectDescriptor getHybrisProjectDescriptor();

    public abstract void setAllModuleList();

    public abstract List<ModuleDescriptor> getBestMatchingExtensionsToImport(@Nullable ProjectSettings settings);

    public abstract void setCoreStepModuleList();

    public abstract void setExternalStepModuleList();

    public abstract void setHybrisModulesToImport(final List<ModuleDescriptor> hybrisModules);

    public abstract List<ModuleDescriptor> getHybrisModulesToImport();

}
