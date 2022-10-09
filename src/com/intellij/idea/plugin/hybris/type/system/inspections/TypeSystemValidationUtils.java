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

package com.intellij.idea.plugin.hybris.type.system.inspections;

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

public abstract class TypeSystemValidationUtils {

    private TypeSystemValidationUtils() {
        //
    }

    public static boolean isCustomExtensionFile(@NotNull final PsiFile file) {
        final VirtualFile vFile = file.getVirtualFile();
        return vFile != null && isCustomExtensionFile(vFile, file.getProject());
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

    /*
     * This method disqualifies known hybris extensions. Anything else is considered for TSV validation.
     */
    protected static boolean estimateIsCustomExtension(@NotNull final VirtualFile file) {
        final File itemsfile = VfsUtilCore.virtualToIoFile(file);
        final String itemsfilePath = normalize(itemsfile.getAbsolutePath());

        if (itemsfilePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX))) {
            return false;
        }
        if (itemsfilePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX_2019))) {
            return false;
        }
        if (itemsfilePath.contains(normalize(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX))) {
            return false;
        }
        return true;
    }

    protected static boolean shouldCheckFilesWithoutHybrisSettings(@NotNull final Project project) {
        // at least it needs to have hybris flag
        final CommonIdeaService commonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService.class);
        return commonIdeaService.isHybrisProject(project);
    }

}
