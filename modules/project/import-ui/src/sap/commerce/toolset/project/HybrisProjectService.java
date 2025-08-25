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

import com.intellij.openapi.components.Service;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenConstants;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.ccv2.CCv2Constants;
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor;
import sap.commerce.toolset.project.vfs.VirtualFileSystemService;

import java.io.File;


@Service
public final class HybrisProjectService {

    public boolean isConfigModule(@NotNull final File file) {
        return new File(file, HybrisConstants.LOCAL_EXTENSIONS_XML).isFile()
            && new File(file, HybrisConstants.LOCAL_PROPERTIES_FILE).isFile();
    }

    public boolean isCCv2Module(@NotNull final File file) {
        return
            (
                file.getAbsolutePath().contains(CCv2Constants.CORE_CUSTOMIZE_NAME)
                    || file.getAbsolutePath().contains(CCv2Constants.DATAHUB_NAME)
                    || file.getAbsolutePath().contains(CCv2Constants.JS_STOREFRONT_NAME)
            )
                && new File(file, CCv2Constants.MANIFEST_NAME).isFile();
    }

    public boolean isAngularModule(@NotNull final File file) {
        return new File(file, HybrisConstants.FILE_ANGULAR_JSON).isFile();
    }

    public boolean isPlatformModule(@NotNull final File file) {
        return file.getName().equals(HybrisConstants.EXTENSION_NAME_PLATFORM)
            && new File(file, HybrisConstants.EXTENSIONS_XML).isFile();
    }

    public boolean isPlatformExtModule(@NotNull final File file) {
        return file.getAbsolutePath().contains(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX)
            && new File(file, HybrisConstants.EXTENSION_INFO_XML).isFile()
            && !isCoreExtModule(file);
    }

    public boolean isCoreExtModule(@NotNull final File file) {
        return file.getAbsolutePath().contains(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX)
            && file.getName().equals(HybrisConstants.EXTENSION_NAME_CORE)
            && new File(file, HybrisConstants.EXTENSION_INFO_XML).isFile();
    }

    public boolean isHybrisModule(@NotNull final File file) {
        return ProjectUtil.isHybrisModuleRoot(file);
    }

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

    public boolean isMavenModule(final File rootProjectDirectory) {
        if (rootProjectDirectory.getAbsolutePath().contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false;
        }
        return new File(rootProjectDirectory, MavenConstants.POM_XML).isFile();
    }

    public boolean isEclipseModule(final File rootProjectDirectory) {
        if (rootProjectDirectory.getAbsolutePath().contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false;
        }
        return new File(rootProjectDirectory, HybrisConstants.DOT_PROJECT).isFile();
    }

    public boolean isGradleModule(final File file) {
        if (file.getAbsolutePath().contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false;
        }
        return new File(file, HybrisConstants.GRADLE_SETTINGS).isFile()
            || new File(file, HybrisConstants.GRADLE_BUILD).isFile();
    }

    public boolean isGradleKtsModule(final File file) {
        if (file.getAbsolutePath().contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) {
            return false;
        }
        return new File(file, HybrisConstants.GRADLE_SETTINGS_KTS).isFile()
            || new File(file, HybrisConstants.GRADLE_BUILD_KTS).isFile();
    }

    public boolean hasVCS(final File rootProjectDirectory) {
        return new File(rootProjectDirectory, ".git").isDirectory()
            || new File(rootProjectDirectory, ".svn").isDirectory()
            || new File(rootProjectDirectory, ".hg").isDirectory();
    }
}
