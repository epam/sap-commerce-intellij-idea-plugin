/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.psi.utils;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.openapi.util.io.FileUtil.normalize;

public final class PsiUtils {

    private PsiUtils() {
    }

    public static String getModuleName(final Module module) {
        final String name = module.getName();
        final String[] names = name.split("\\.");
        return names[names.length - 1];
    }

    public static Module getModule(final @NotNull PsiFile file) {
        final VirtualFile vFile = file.getVirtualFile();

        if (vFile == null) {
            return null;
        }

        return ModuleUtilCore.findModuleForFile(vFile, file.getProject());
    }

    public static boolean isCustomExtensionFile(@NotNull final VirtualFile file, @NotNull final Project project) {
        final Module module = ModuleUtilCore.findModuleForFile(file, project);

        if (null == module) {
            return false;
        }
        final String descriptorTypeName = module.getOptionValue(HybrisConstants.DESCRIPTOR_TYPE);

        if (descriptorTypeName == null) {
            if (shouldCheckFilesWithoutHybrisSettings(project)) {
                return estimateIsCustomExtension(file);
            }
            return false;
        }

        final HybrisModuleDescriptorType descriptorType = HybrisModuleDescriptorType.valueOf(descriptorTypeName);
        return descriptorType == HybrisModuleDescriptorType.CUSTOM;
    }

    private static boolean shouldCheckFilesWithoutHybrisSettings(@NotNull final Project project) {
        // at least it needs to have hybris flag
        final CommonIdeaService commonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService.class);
        return commonIdeaService.isHybrisProject(project);
    }

    private static boolean estimateIsCustomExtension(@NotNull final VirtualFile file) {
        final File itemsFile = VfsUtilCore.virtualToIoFile(file);
        final String filePath = normalize(itemsFile.getAbsolutePath());

        if (filePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX))) {
            return false;
        }
        if (filePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX_2019))) {
            return false;
        }
        if (filePath.contains(normalize(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX))) {
            return false;
        }
        return true;
    }
}
