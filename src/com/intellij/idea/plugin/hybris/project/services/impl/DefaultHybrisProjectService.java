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

package com.intellij.idea.plugin.hybris.project.services.impl;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.HybrisUtil;
import com.intellij.idea.plugin.hybris.common.services.VirtualFileSystemService;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.services.HybrisProjectService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenConstants;

import java.io.File;

public class DefaultHybrisProjectService implements HybrisProjectService {

    @Override
    public boolean isConfigModule(@NotNull final File file) {
        return new File(file, HybrisConstants.LOCAL_EXTENSIONS_XML).isFile()
               && new File(file, HybrisConstants.LOCAL_PROPERTIES_FILE).isFile();
    }

    @Override
    public boolean isCCv2Module(@NotNull final File file) {
        return
            (
                file.getAbsolutePath().contains(HybrisConstants.CCV2_CORE_CUSTOMIZE_NAME)
                || file.getAbsolutePath().contains(HybrisConstants.CCV2_DATAHUB_NAME)
                || file.getAbsolutePath().contains(HybrisConstants.CCV2_JS_STOREFRONT_NAME)
            )
            && new File(file, HybrisConstants.CCV2_MANIFEST_NAME).isFile();
    }

    @Override
    public boolean isAngularModule(@NotNull final File file) {
        return new File(file, HybrisConstants.FILE_ANGULAR_JSON).isFile();
    }

    @Override
    public boolean isPlatformModule(@NotNull final File file) {
        return file.getName().equals(HybrisConstants.EXTENSION_NAME_PLATFORM)
               && new File(file, HybrisConstants.EXTENSIONS_XML).isFile();
    }

    @Override
    public boolean isPlatformExtModule(@NotNull final File file) {
        return file.getAbsolutePath().contains(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX)
               && new File(file, HybrisConstants.EXTENSION_INFO_XML).isFile()
               && !isCoreExtModule(file);
    }

    @Override
    public boolean isCoreExtModule(@NotNull final File file) {
        return file.getAbsolutePath().contains(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX)
               && file.getName().equals(HybrisConstants.EXTENSION_NAME_CORE)
               && new File(file, HybrisConstants.EXTENSION_INFO_XML).isFile();
    }

    @Override
    public boolean isHybrisModule(@NotNull final File file) {
        return HybrisUtil.isHybrisModuleRoot(file);
    }

    @Override
    public boolean isOutOfTheBoxModule(@NotNull final File file, final HybrisProjectDescriptor rootProjectDescriptor) {
        final File extDir = rootProjectDescriptor.getExternalExtensionsDirectory();
        if (extDir != null) {
            if (VirtualFileSystemService.getInstance().fileContainsAnother(extDir, file)) {
                // this will override bin/ext-* naming convention.
                return false;
            }
        }
        return (file.getAbsolutePath().contains(HybrisConstants.PLATFORM_OOTB_MODULE_PREFIX) ||
                file.getAbsolutePath().contains(HybrisConstants.PLATFORM_OOTB_MODULE_PREFIX_2019)
               )
               && new File(file, HybrisConstants.EXTENSION_INFO_XML).isFile();
    }

    @Override
    public boolean isMavenModule(final File rootProjectDirectory) {
        if (rootProjectDirectory.getAbsolutePath().contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false;
        }
        return new File(rootProjectDirectory, MavenConstants.POM_XML).isFile();
    }

    @Override
    public boolean isEclipseModule(final File rootProjectDirectory) {
        if (rootProjectDirectory.getAbsolutePath().contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false;
        }
        return new File(rootProjectDirectory, HybrisConstants.DOT_PROJECT).isFile();
    }

    @Override
    public boolean isGradleModule(final File file) {
        if (file.getAbsolutePath().contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false;
        }
        return new File(file, HybrisConstants.GRADLE_SETTINGS).isFile()
            || new File(file, HybrisConstants.GRADLE_BUILD).isFile();
    }

    @Override
    public boolean isGradleKtsModule(final File file) {
        if (file.getAbsolutePath().contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false;
        }
        return new File(file, HybrisConstants.GRADLE_SETTINGS_KTS).isFile()
            || new File(file, HybrisConstants.GRADLE_BUILD_KTS).isFile();
    }

    @Override
    public boolean hasVCS(final File rootProjectDirectory) {
        return new File(rootProjectDirectory, ".git").isDirectory()
            || new File(rootProjectDirectory, ".svn").isDirectory()
            || new File(rootProjectDirectory, ".hg").isDirectory();
    }
}
