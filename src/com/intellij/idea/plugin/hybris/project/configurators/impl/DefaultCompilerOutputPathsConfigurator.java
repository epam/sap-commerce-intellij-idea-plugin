/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.CompilerOutputPathsConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtilCore;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;

public class DefaultCompilerOutputPathsConfigurator implements CompilerOutputPathsConfigurator {

    @Override
    public void configure(
        @NotNull final ProgressIndicator indicator,
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final ModuleDescriptor moduleDescriptor
    ) {
        indicator.setText2(message("hybris.project.import.module.outputpath"));

        final CompilerModuleExtension compilerModuleExtension = modifiableRootModel.getModuleExtension(
            CompilerModuleExtension.class
        );

        final File outputDirectory = new File(
            moduleDescriptor.getModuleRootDirectory(),
            HybrisConstants.JAVA_COMPILER_FAKE_OUTPUT_PATH
        );

        compilerModuleExtension.setCompilerOutputPath(VfsUtilCore.pathToUrl(outputDirectory.getAbsolutePath()));
        compilerModuleExtension.setCompilerOutputPathForTests(VfsUtilCore.pathToUrl(outputDirectory.getAbsolutePath()));

        compilerModuleExtension.setExcludeOutput(true);
        compilerModuleExtension.inheritCompilerOutputPath(false);
    }

}
