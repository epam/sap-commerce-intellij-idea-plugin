/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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

package com.intellij.idea.plugin.hybris.psi.util;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType;
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;

import static com.intellij.openapi.util.io.FileUtil.normalize;

public final class PsiUtils {

    private PsiUtils() {
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
        final var settingsComponent = ProjectSettingsComponent.getInstance(project);
        final var descriptorType = settingsComponent.getModuleSettings(module).getType();

        if (descriptorType == ModuleDescriptorType.NONE) {
            if (shouldCheckFilesWithoutHybrisSettings(project)) {
                return estimateIsCustomExtension(file) == ModuleDescriptorType.CUSTOM;
            }
            return false;
        }

        return descriptorType == ModuleDescriptorType.CUSTOM;
    }

    private static boolean shouldCheckFilesWithoutHybrisSettings(@NotNull final Project project) {
        // at least it needs to have a hybris flag
        return ProjectSettingsComponent.getInstance(project).isHybrisProject();
    }

    private static ModuleDescriptorType estimateIsCustomExtension(@NotNull final VirtualFile file) {
        final File itemsFile = VfsUtilCore.virtualToIoFile(file);
        final String filePath = normalize(itemsFile.getAbsolutePath());

        if (filePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX))) {
            return ModuleDescriptorType.OOTB;
        }
        if (filePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX_2019))) {
            return ModuleDescriptorType.OOTB;
        }
        if (filePath.contains(normalize(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX))) {
            return ModuleDescriptorType.EXT;
        }
        return ModuleDescriptorType.CUSTOM;
    }

    public static boolean shouldCreateNewReference(final @Nullable PsiReference reference, final String text) {
        if (reference == null) return true;

        if (reference instanceof final PsiReferenceBase psiReferenceBase) {
            return text != null
                && (text.length() != reference.getRangeInElement().getLength() || !text.equals(psiReferenceBase.getValue()));
        } else {
            return false;
        }
    }

    @NotNull
    public static ResolveResult[] getValidResults(final ResolveResult[] resolveResults) {
        return Arrays.stream(resolveResults)
            .filter(ResolveResult::isValidResult)
            .toArray(ResolveResult[]::new);
    }
}
